package com.smartcane.api.domain.billing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcane.api.domain.billing.dto.AutoPayRequest;
import com.smartcane.api.domain.billing.dto.AutoPayResponse;
import com.smartcane.api.domain.billing.dto.BillingKeyIssueRequest;
import com.smartcane.api.domain.billing.dto.BillingKeyIssueResponse;
import com.smartcane.api.domain.billing.dto.CancelRequest;
import com.smartcane.api.domain.billing.dto.PaymentWebhookEvent;
import com.smartcane.api.domain.billing.dto.WebhookEvent;
import com.smartcane.api.domain.billing.entity.BillingProfile;
import com.smartcane.api.domain.billing.entity.PaymentLedger;
import com.smartcane.api.domain.billing.entity.WebhookReceipt;
import com.smartcane.api.domain.billing.repository.BillingProfileRepository;
import com.smartcane.api.domain.billing.repository.PaymentLedgerRepository;
import com.smartcane.api.domain.billing.repository.WebhookReceiptRepository;
import com.smartcane.api.domain.billing.util.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingProfileRepository repo;
    private final TossBillingClient toss;
    private final WebhookVerifier webhookVerifier;

    private final WebhookReceiptRepository webhookReceiptRepo;  // ✅ 중복처리용
    private final PaymentLedgerRepository ledgerRepo;           // ✅ 정산/거래 로그

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public BillingKeyIssueResponse issueBillingKey(BillingKeyIssueRequest req) {
        Map<String, Object> res = toss.issueBillingKey(req.authKeyOrToken(), req.customerKey());

        String billingKey  = (String) res.get("billingKey");
        Map<String, Object> card = (Map<String, Object>) res.get("card");
        String cardCompany = card != null ? (String) card.get("company") : "UNKNOWN";
        String masked      = card != null ? (String) card.get("number") : "****-****";

        BillingProfile profile = repo.findByCustomerKey(req.customerKey())
                .orElse(BillingProfile.builder()
                        .userId(req.userId())
                        .customerKey(req.customerKey())
                        .createdAt(OffsetDateTime.now())
                        .build());

        profile.setBillingKey(billingKey);
        profile.setStatus(BillingProfile.BillingStatus.ACTIVE);
        profile.setUpdatedAt(OffsetDateTime.now());
        repo.save(profile);

        return new BillingKeyIssueResponse(billingKey, cardCompany, masked);
    }

    @Override
    @Transactional
    public AutoPayResponse autoPay(AutoPayRequest req) {
        Map<String, Object> body = Map.of(
                "customerKey", req.customerKey(),
                "orderId", req.orderId(),
                "orderName", req.orderName(),
                "amount", req.amount(),
                "currency", req.currency() == null ? "KRW" : req.currency()
        );

        Map<String, Object> res = toss.requestPayment(req.billingKey(), body);

        String paymentKey = (String) res.get("paymentKey");
        String status     = (String) res.getOrDefault("status", "UNKNOWN");
        String approvedAt = (String) res.get("approvedAt");

        Map<String, Object> card = (Map<String, Object>) res.get("card");
        String issuerCode  = card != null ? (String) card.get("issuerCode") : null;

        return new AutoPayResponse(paymentKey, status, issuerCode,
                approvedAt != null ? OffsetDateTime.parse(approvedAt) : OffsetDateTime.now(),
                null);
    }

    @Override
    @Transactional
    public void cancel(CancelRequest req) {
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", req.cancelReason());
        // 부분취소/세금비과세/환불계좌 등 필요 시 body 필드 확장
        toss.cancelPayment(req.paymentKey(), body);
    }

    /**
     * ✅ 중복처리(DB UNIQUE) + Ledger 반영 병합 버전
     */
    @Override
    @Transactional
    public void handleWebhook(WebhookEvent event) {
        // (있으면) 서명 검증: payout.changed, seller.changed 등 서명 헤더가 포함되는 유형만 검증됨
        webhookVerifier.verifyOrThrow(event);

        // transmissionId가 없을 수도 있으므로 대체 키 구성 (paymentKey|eventType|orderId)
        String txId = event.transmissionId();
        if (txId == null || txId.isBlank()) {
            txId = String.join("|",
                    nullToDash(event.paymentKey()),
                    nullToDash(event.eventType()),
                    nullToDash(event.orderId()));
        }

        // ✅ 1) 먼저 receipt 저장 → UNIQUE 제약으로 중복 차단
        try {
            webhookReceiptRepo.save(WebhookReceipt.builder()
                    .provider("TOSS")
                    .transmissionId(txId)
                    .eventType(event.eventType())
                    .receivedAt(OffsetDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException dup) {
            // 이미 처리된 웹훅 → 스킵
            return;
        }

        // ✅ 2) 비즈니스 처리 (Ledger 반영)
        try {
            // eventType이 payment.* 인 경우, PaymentWebhookEvent DTO로 파싱하여 처리
            if (event.eventType() != null && event.eventType().startsWith("payment.")) {
                PaymentWebhookEvent pay = objectMapper.readValue(event.rawBody(), PaymentWebhookEvent.class);

                switch (event.eventType()) {
                    case "payment.approved" -> processPaymentApproved(pay);
                    case "payment.canceled" -> processPaymentCanceled(pay);
                    default -> { /* 필요 시 로깅 */ }
                }

            } else if ("billingKey.deleted".equals(event.eventType())) {
                // 기존 BillingProfile 상태 변경
                JsonNode root = objectMapper.readTree(event.rawBody());
                String customerKey = getText(root, "customerKey", null);
                if (customerKey != null) {
                    repo.findByCustomerKey(customerKey).ifPresent(p -> {
                        p.setStatus(BillingProfile.BillingStatus.REVOKED);
                        p.setUpdatedAt(OffsetDateTime.now());
                        repo.save(p);
                    });
                }
            } else {
                // 기타 이벤트: 로깅만
            }

        } catch (Exception e) {
            // 비즈니스 처리 실패 시 트랜잭션 롤백 → receipt 저장도 롤백되어
            // 동일 txId 재전송이 오면 재처리 가능
            throw new IllegalArgumentException("Webhook handling failed", e);
        }
    }

    /* ===================== Ledger 반영 로직 ===================== */

    private void processPaymentApproved(PaymentWebhookEvent pay) {
        // orderId 기준으로 ledger upsert (정책에 따라 paymentKey 기준 사용 가능)
        PaymentLedger lg = ledgerRepo.findByOrderId(pay.orderId())
                .orElseGet(() -> PaymentLedger.builder()
                        .orderId(pay.orderId())
                        .currency(pay.currency() == null ? "KRW" : pay.currency())
                        .build());

        lg.setPaymentKey(pay.paymentKey());
        lg.setOrderName(pay.orderName());
        lg.setAmount(pay.effectiveAmount() != null ? pay.effectiveAmount() : BigDecimal.ZERO);
        lg.setMethod(pay.method());
        if (pay.card() != null) {
            lg.setIssuerCode(pay.card().issuerCode());
            lg.setAcquirerCode(pay.card().acquirerCode());
        }
        lg.setStatus(PaymentLedger.Status.DONE);
        lg.setApprovedAt(pay.approvedAt());

        ledgerRepo.save(lg);
    }

    private void processPaymentCanceled(PaymentWebhookEvent pay) {
        PaymentLedger lg = ledgerRepo.findByOrderId(pay.orderId())
                .orElseGet(() -> PaymentLedger.builder()
                        .orderId(pay.orderId())
                        .currency(pay.currency() == null ? "KRW" : pay.currency())
                        .build());

        // 부분취소 누적 금액 계산
        BigDecimal canceledSum = BigDecimal.ZERO;
        if (pay.cancels() != null) {
            for (var c : pay.cancels()) {
                if (c.cancelAmount() != null) {
                    canceledSum = canceledSum.add(c.cancelAmount());
                }
            }
        }

        // 원승인 금액이 없으면 응답 금액으로 채움(최초 취소 이벤트만 온 경우 대비)
        if (lg.getAmount() == null || lg.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            lg.setAmount(pay.effectiveAmount() != null ? pay.effectiveAmount() : BigDecimal.ZERO);
        }

        // 상태 전이
        if (lg.getAmount() != null && canceledSum.compareTo(lg.getAmount()) >= 0) {
            lg.setStatus(PaymentLedger.Status.CANCELED);
            lg.setCanceledAt(pay.canceledAt());
        } else {
            lg.setStatus(PaymentLedger.Status.PARTIAL_CANCELED);
        }

        ledgerRepo.save(lg);
    }

    /* ======================== 유틸 ======================== */

    private String getText(JsonNode root, String key, String defaultVal) {
        JsonNode n = root.get(key);
        return (n != null && !n.isNull()) ? n.asText() : defaultVal;
    }

    private String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
