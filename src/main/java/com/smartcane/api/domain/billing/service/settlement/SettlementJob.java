package com.smartcane.api.domain.billing.service.settlement;

import com.smartcane.api.domain.billing.dto.AutoPayRequest;
import com.smartcane.api.domain.billing.dto.fare.JourneyCharge;
import com.smartcane.api.domain.billing.dto.fare.RideContext;
import com.smartcane.api.domain.billing.service.BillingService;
import com.smartcane.api.domain.billing.service.InvoiceService;
import com.smartcane.api.domain.billing.service.fare.TransferFareService;
import com.smartcane.api.domain.billing.entity.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJob {

    private final RideLogPort rideLogPort;
    private final TransferFareService transferFareService;
    private final InvoiceService invoiceService;
    private final BillingService billingService;

    /** 매일 새벽 03:10, 전일(00:00~24:00) 정산 */
    @Scheduled(cron = "0 10 3 * * *", zone = "Asia/Seoul")
    public void runDailySettlement() {
        var zone = ZoneId.of("Asia/Seoul");
        var to   = OffsetDateTime.now(zone).withHour(0).withMinute(0).withSecond(0).withNano(0); // 오늘 00:00
        var from = to.minusDays(1); // 어제 00:00 ~ 오늘 00:00

        log.info("[Settlement] From {} To {}", from, to);

        List<RideContext> rides = rideLogPort.findRides(from, to);
        if (rides.isEmpty()) {
            log.info("[Settlement] No rides.");
            return;
        }

        // 사용자별 그룹
        Map<Long, List<RideContext>> byUser = rides.stream()
                .collect(Collectors.groupingBy(RideContext::userId));

        byUser.forEach((userId, userRides) -> {
            // 1) 여정 묶음 → 금액 계산
            var journeys = transferFareService.settleJourneys(userId, userRides);

            for (JourneyCharge j : journeys) {
                // 2) 인보이스 upsert
                var inv = invoiceService.upsertFromJourney(j);

                // 3) 자동결제 호출(멱등: orderId 동일)
                try {
                    String customerKey = rideLogPort.findCustomerKeyByUserId(userId);
                    String billingKey  = rideLogPort.findBillingKeyByUserId(userId);

                    invoiceService.markPaying(inv.getOrderId());
                    AutoPayRequest req = invoiceService.toAutoPayRequest(inv, customerKey, billingKey);
                    var res = billingService.autoPay(req);

                    // 승인 응답이 즉시 성공이면 PAID로 선반영(웹훅에서도 다시 동기화)
                    if ("DONE".equalsIgnoreCase(res.status())) {
                        invoiceService.markPaid(inv.getOrderId(), res.paymentKey());
                    }
                } catch (Exception e) {
                    // 실패 시 FAILED 표시 → 다음 배치에서 재시도 전략 수립 가능
                    invoiceService.markFailedOrCanceled(inv.getOrderId(), false);
                    log.error("[Settlement] autoPay failed for orderId={}", inv.getOrderId(), e);
                }
            }
        });
    }
}
