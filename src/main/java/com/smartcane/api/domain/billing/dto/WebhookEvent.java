package com.smartcane.api.domain.billing.dto;

public record WebhookEvent(
        String eventType,        // 예: payment.approved, billingKey.deleted, payout.changed ...
        String paymentKey,
        String orderId,
        String status,
        String signature,        // tosspayments-webhook-signature (없는 케이스도 있음)
        String transmissionTime, // tosspayments-webhook-transmission-time
        String transmissionId,   // tosspayments-webhook-transmission-id
        String retriedCount,     // tosspayments-webhook-transmission-retried-count
        String rawBody           // 원문 payload
) {}
