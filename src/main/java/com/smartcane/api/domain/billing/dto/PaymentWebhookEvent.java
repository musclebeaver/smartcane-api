package com.smartcane.api.domain.billing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookEvent(
        // 웹훅 상단 메타(있을 수도 있고 없을 수도 있음)
        String eventType,          // 예: "payment.approved", "payment.canceled"
        String version,            // optional
        // 핵심 결제 식별자
        String paymentKey,
        String orderId,
        String orderName,
        String status,             // 예: READY, IN_PROGRESS, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED
        String method,             // 예: CARD, VIRTUAL_ACCOUNT, EASY_PAY, TRANSFER ...
        BigDecimal totalAmount,    // 일부 응답에선 amount 로도 내려올 수 있음 → 아래 @JsonProperty로 보정
        @JsonProperty("amount") BigDecimal amountAlias, // 있으면 totalAmount로 보정해 사용
        String currency,           // 보통 "KRW"

        // 카드/간편결제 등 수단별 상세
        CardInfo card,
        EasyPayInfo easyPay,
        VirtualAccountInfo virtualAccount,

        // 승인/취소 시각
        OffsetDateTime approvedAt,
        OffsetDateTime requestedAt,
        OffsetDateTime canceledAt,

        // 다건 취소 내역
        List<Cancellation> cancels,

        // 부가 정보(영수증, VAT, 공급가 등) – 문서마다 유동적이라 Map으로 받도록
        Map<String, Object> metadata
) {
    // 편의 getter: amountAlias가 있으면 우선, 없으면 totalAmount 사용
    public BigDecimal effectiveAmount() {
        return amountAlias != null ? amountAlias : totalAmount;
    }
}
