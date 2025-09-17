package com.smartcane.api.domain.billing.service;

import com.smartcane.api.domain.billing.dto.AutoPayRequest;
import com.smartcane.api.domain.billing.dto.fare.JourneyCharge;
import com.smartcane.api.domain.billing.entity.Invoice;
import com.smartcane.api.domain.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository repo;

    /** JourneyCharge → Invoice 영속화 (멱등: orderId unique) */
    @Transactional
    public Invoice upsertFromJourney(JourneyCharge j) {
        return repo.findByOrderId(j.orderId()).orElseGet(() -> {
            var inv = Invoice.builder()
                    .userId(j.userId())
                    .orderId(j.orderId())
                    .title("교통요금(" + j.startedAt() + " ~ " + j.endedAt() + ")")
                    .amount(j.breakdown().total())
                    .currency(j.breakdown().currency())
                    .status(Invoice.Status.PENDING)
                    .billedFrom(j.startedAt())
                    .billedTo(j.endedAt())
                    .build();
            return repo.save(inv);
        });
    }

    /** 자동결제 전 상태 전이 */
    @Transactional
    public void markPaying(String orderId) {
        repo.findByOrderId(orderId).ifPresent(inv -> inv.setStatus(Invoice.Status.PAYING));
    }

    /** 결제 성공 웹훅/응답 처리 */
    @Transactional
    public void markPaid(String orderId, String paymentKey) {
        repo.findByOrderId(orderId).ifPresent(inv -> {
            inv.setStatus(Invoice.Status.PAID);
            inv.setPaymentKey(paymentKey);
        });
    }

    /** 결제 실패/취소 처리 */
    @Transactional
    public void markFailedOrCanceled(String orderId, boolean canceled) {
        repo.findByOrderId(orderId).ifPresent(inv -> {
            inv.setStatus(canceled ? Invoice.Status.CANCELED : Invoice.Status.FAILED);
        });
    }

    /** Invoice → AutoPayRequest 매핑 */
    public AutoPayRequest toAutoPayRequest(Invoice inv, String customerKey, String billingKey) {
        return new AutoPayRequest(
                inv.getUserId(),
                customerKey,
                billingKey,
                inv.getOrderId(),
                inv.getTitle(),
                inv.getAmount(),
                inv.getCurrency()
        );
    }
}
