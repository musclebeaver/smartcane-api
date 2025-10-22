package com.smartcane.api.domain.point.dto;

import jakarta.validation.constraints.Positive;

/**
 * 포인트 결제 시 필요한 최소한의 요청 파라미터만 정의한 DTO입니다.
 */
public record PointPaymentRequest(
        @Positive(message = "결제 금액은 0보다 커야 합니다.") long amount
) {
}
