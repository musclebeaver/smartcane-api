package com.smartcane.api.domain.billing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Cancellation(
        String cancelReason,
        BigDecimal cancelAmount,
        BigDecimal taxFreeAmount,
        OffsetDateTime canceledAt,
        String transactionKey // 부분취소 트랜잭션 키 등
) {}
