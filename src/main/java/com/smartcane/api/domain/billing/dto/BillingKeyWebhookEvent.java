package com.smartcane.api.domain.billing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BillingKeyWebhookEvent(
        String eventType,      // 예: "billingKey.deleted"
        String customerKey,
        String billingKey,
        String reason,         // 삭제 사유 등(있다면)
        OffsetDateTime occurredAt
) {}
