package com.smartcane.api.domain.billing.service.fare;

import com.smartcane.api.domain.billing.config.FareProperties;
import com.smartcane.api.domain.billing.dto.fare.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferFareService {

    private static final String TYPE_BASE = "BASE";
    private static final String TYPE_TRANSFER = "TRANSFER";
    private static final String TYPE_DISCOUNT_ACCESSIBLE = "DISCOUNT_ACCESSIBLE";
    private static final String TYPE_DISCOUNT_SUBSCRIPTION = "DISCOUNT_SUBSCRIPTION";

    private final FareProperties props;
    private final FareCalculator fareCalculator;

    /**
     * 같은 사용자에 대한 탑승 리스트를 환승 창구 기준으로 묶어,
     * 여정(Journey) 단위로 최종 청구 결과를 만든다.
     */
    public List<JourneyCharge> settleJourneys(Long userId, List<RideContext> rides) {
        if (userId == null || rides == null || rides.isEmpty()) return List.of();

        // 1) 사용자/시간 기준 정렬 및 필터
        List<RideContext> sorted = rides.stream()
                .filter(r -> Objects.equals(r.userId(), userId))
                .sorted(Comparator.comparing(RideContext::startedAt))
                .collect(Collectors.toList());

        if (sorted.isEmpty()) return List.of();

        // 2) 환승 창구 기준 그룹핑
        List<List<RideContext>> groups = groupByTransferWindow(sorted);

        // 3) 각 그룹(여정)에 대해 금액 산출
        List<JourneyCharge> result = new ArrayList<>(groups.size());
        for (List<RideContext> group : groups) {
            result.add(priceJourney(group));
        }
        return result;
    }

    /* ===== 내부 로직 ===== */

    private List<List<RideContext>> groupByTransferWindow(List<RideContext> rides) {
        var tf = props.getTransfer();
        if (tf == null || !tf.isEnabled()) {
            return rides.stream().map(List::of).collect(Collectors.toList());
        }

        final int window = Math.max(0, tf.getWindowMinutes()); // 음수 방지
        final int maxTransfers = Math.max(0, tf.getMaxTransfers()); // 음수 방지

        List<List<RideContext>> groups = new ArrayList<>();
        List<RideContext> cur = new ArrayList<>();

        for (RideContext r : rides) {
            if (cur.isEmpty()) {
                cur.add(r);
                continue;
            }
            RideContext prev = cur.get(cur.size() - 1);
            long gapMin = Duration.between(prev.endedAt(), r.startedAt()).toMinutes();

            // 환승 가능 조건:
            // - 시간 간격 0 이상 window 이내
            // - 이미 누적된 환승 횟수(cur.size()-1)가 maxTransfers 미만
            boolean withinWindow = gapMin >= 0 && gapMin <= window;
            boolean transferSlotsLeft = (cur.size() - 1) < maxTransfers;

            if (withinWindow && transferSlotsLeft) {
                cur.add(r);
            } else {
                groups.add(cur);
                cur = new ArrayList<>();
                cur.add(r);
            }
        }
        if (!cur.isEmpty()) groups.add(cur);
        return groups;
    }

    private JourneyCharge priceJourney(List<RideContext> group) {
        // 여정 시간 범위
        var startedAt = group.get(0).startedAt();
        var endedAt   = group.get(group.size()-1).endedAt();
        Long userId   = group.get(0).userId();

        // 1) 각 탑승의 “기본 요금(BASE)” 계산 (단일 탑승 계산기 재사용)
        List<FareItem> items = new ArrayList<>();
        // 각 탑승의 BASE 금액을 캐싱(추후 환승 보정 계산에 사용)
        List<BigDecimal> basePerRide = new ArrayList<>(group.size());

        for (RideContext r : group) {
            var calc = fareCalculator.calculate(r);

            // 단일 계산기 결과에서 BASE 항목만 추출 (해당 계산기가 할인 등을 포함해 오더라도
            // 여정 단위에서 다시 보정하므로 여기서는 BASE만 취함)
            BigDecimal baseOnly = calc.items().stream()
                    .filter(it -> TYPE_BASE.equals(it.type()))
                    .findFirst()
                    .map(FareItem::amount)
                    .orElse(BigDecimal.ZERO);

            basePerRide.add(nonNull(baseOnly));

            String desc = (r.mode() == TransportMode.BUS ? "버스" : "지하철") + " 기본요금";
            items.add(new FareItem(TYPE_BASE, desc, nonNull(baseOnly)));
        }

        // 2) 환승 가격 정책 적용: 첫 탑승은 BASE 그대로, 이후 탑승은 "원래 BASE → 환승가"로 보정
        var tf = props.getTransfer();
        if (tf != null && tf.isEnabled() && group.size() > 1 && tf.getPricing() != null) {
            var pricing = tf.getPricing();

            for (int i = 1; i < group.size(); i++) {
                RideContext r = group.get(i);

                // orig: i번째 탑승의 원래 BASE 금액 (계산 결과를 신뢰)
                BigDecimal orig = basePerRide.get(i);

                // 환승가 계산
                BigDecimal transferFare = switch (pricing.getType()) {
                    case FREE -> BigDecimal.ZERO;
                    case FLAT -> bd(pricing.getFlatAmount());
                    case RATIO -> currencyRound(orig.multiply(bd(pricing.getRatio())));
                };

                // 보정 금액 = (환승가 - 원래 BASE)
                // 이를 별도 TRANSFER 항목으로 넣어 전체 합계를 환승가로 맞춘다.
                BigDecimal adjust = transferFare.subtract(orig);
                items.add(new FareItem(
                        TYPE_TRANSFER,
                        "환승 (" + (r.mode()==TransportMode.BUS ? "버스" : "지하철") + ")",
                        adjust
                ));
            }
        }

        // 3) 여정 기준 할인(교통약자/구독)은 “여정 총액”에 대해 적용
        BigDecimal subtotal = sum(items);        // BASE + TRANSFER 보정의 합
        BigDecimal surcharges = BigDecimal.ZERO; // 현재 별도 할증 없음
        BigDecimal discounts = BigDecimal.ZERO;

        boolean accessible = group.stream().anyMatch(RideContext::accessibleUser);
        boolean subscribed = group.stream().anyMatch(RideContext::hasSubscription);

        if (accessible && props.getDiscounts() != null && props.getDiscounts().getAccessibleUserRate() > 0) {
            BigDecimal d = subtotal.multiply(bd(props.getDiscounts().getAccessibleUserRate())).negate();
            d = currencyRound(d);
            if (d.signum() != 0) {
                items.add(new FareItem(TYPE_DISCOUNT_ACCESSIBLE, "교통약자 할인(여정)", d));
                discounts = discounts.add(d);
            }
        }
        if (subscribed && props.getDiscounts() != null && props.getDiscounts().getSubscriptionRate() > 0) {
            BigDecimal d = subtotal.multiply(bd(props.getDiscounts().getSubscriptionRate())).negate();
            d = currencyRound(d);
            if (d.signum() != 0) {
                items.add(new FareItem(TYPE_DISCOUNT_SUBSCRIPTION, "구독 할인(여정)", d));
                discounts = discounts.add(d);
            }
        }

        BigDecimal total = roundTo10Won(subtotal.add(surcharges).add(discounts));

        // orderId는 여정 식별 규칙에 맞게 생성(예: userId-시작시각-끝시각)
        String orderId = "JNY-" + userId + "-" + startedAt.toEpochSecond() + "-" + endedAt.toEpochSecond();

        return JourneyCharge.builder()
                .userId(userId)
                .orderId(orderId)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .rides(group)
                .breakdown(FareBreakdown.builder()
                        .currency(props.getCurrency())
                        .subtotal(subtotal)
                        .surcharges(surcharges)
                        .discounts(discounts)
                        .total(total)
                        .items(items)
                        .build())
                .build();
    }

    /* ===== helpers ===== */

    private static BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 10원 단위 반올림 */
    private BigDecimal roundTo10Won(BigDecimal v) {
        return v.divide(bd(10), 0, RoundingMode.HALF_UP).multiply(bd(10));
    }
    /** 통화 기본 반올림 (일원 단위) */
    private BigDecimal currencyRound(BigDecimal v) { return v.setScale(0, RoundingMode.HALF_UP); }
    private BigDecimal bd(double v) { return BigDecimal.valueOf(v); }
    private BigDecimal sum(List<FareItem> items) {
        return items.stream().map(FareItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
