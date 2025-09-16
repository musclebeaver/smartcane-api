package com.smartcane.api.domain.billing.dto;

import java.time.OffsetDateTime;

public record AutoPayResponse(
        String paymentKey,
        String status,
        String approvedCardCode,
        OffsetDateTime approvedAt,
        String failureReason
) {}