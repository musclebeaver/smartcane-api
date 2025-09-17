package com.smartcane.api.domain.billing.service.fare;

import com.smartcane.api.domain.billing.config.FareProperties;
import com.smartcane.api.domain.billing.dto.fare.FareBreakdown;
import com.smartcane.api.domain.billing.dto.fare.FareItem;
import com.smartcane.api.domain.billing.dto.fare.RideContext;
import com.smartcane.api.domain.billing.dto.fare.TransportMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FareCalculatorImpl implements FareCalculator {

    private final FareProperties props;

    @Override
    public FareBreakdown calculate(RideContext ride) {
        List<FareItem> items = new ArrayList<>();

        // ✅ 모드별 고정 요금
        BigDecimal base = switch (ride.mode()) {
            case BUS -> bd(props.getBusFare());
            case SUBWAY -> bd(props.getSubwayFare());
        };
        items.add(new FareItem("BASE", ride.mode() == TransportMode.BUS ? "버스 기본요금" : "지하철 기본요금", base));

        // ✅ 가산 없음, 시간/거리 없음
        BigDecimal subtotal = base;
        BigDecimal surcharges = BigDecimal.ZERO;

        // ✅ 할인만 적용(옵션)
        BigDecimal discounts = BigDecimal.ZERO;
        if (ride.accessibleUser() && props.getDiscounts().getAccessibleUserRate() > 0) {
            BigDecimal d = subtotal.multiply(bd(props.getDiscounts().getAccessibleUserRate())).negate();
            d = currencyRound(d);
            if (d.signum() < 0) {
                items.add(new FareItem("DISCOUNT_ACCESSIBLE", "교통약자 할인", d));
                discounts = discounts.add(d);
            }
        }
        if (ride.hasSubscription() && props.getDiscounts().getSubscriptionRate() > 0) {
            BigDecimal d = subtotal.multiply(bd(props.getDiscounts().getSubscriptionRate())).negate();
            d = currencyRound(d);
            if (d.signum() < 0) {
                items.add(new FareItem("DISCOUNT_SUBSCRIPTION", "구독 할인", d));
                discounts = discounts.add(d);
            }
        }

        // ✅ 최종 금액 (10원 단위 반올림 예시)
        BigDecimal total = subtotal.add(surcharges).add(discounts);
        total = roundTo10Won(total);

        return FareBreakdown.builder()
                .currency(props.getCurrency())
                .subtotal(subtotal)
                .surcharges(surcharges)   // 항상 0
                .discounts(discounts)     // 음수
                .total(total)
                .items(items)
                .build();
    }

    /* ===== helpers ===== */

    private BigDecimal roundTo10Won(BigDecimal v) {
        return v.divide(bd(10), 0, RoundingMode.HALF_UP).multiply(bd(10));
    }
    private BigDecimal currencyRound(BigDecimal v) { return v.setScale(0, RoundingMode.HALF_UP); }
    private BigDecimal bd(double v) { return BigDecimal.valueOf(v); }
}
