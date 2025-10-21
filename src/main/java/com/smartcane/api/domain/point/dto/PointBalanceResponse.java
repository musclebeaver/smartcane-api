package com.smartcane.api.domain.point.dto;

/**
 * 현재 포인트 잔액을 프론트엔드에 전달하기 위한 응답 전용 DTO 입니다.
 */
public record PointBalanceResponse(long balance) {
}
