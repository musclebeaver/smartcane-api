package com.smartcane.api.domain.point.dto;

import jakarta.validation.constraints.Positive;

/**
 * 포인트 충전 금액을 전달 받기 위한 요청 DTO 입니다.
 */
public record PointChargeRequest(@Positive(message = "충전 금액은 0보다 커야 합니다.") long amount) {
}
