package com.smartcane.api.domain.point.exception;

/**
 * 포인트 결제 또는 차감 시 잔고가 부족한 경우 발생하는 예외입니다.
 */
public class PointInsufficientBalanceException extends PointException {

    public PointInsufficientBalanceException(long balance, long required) {
        super(String.format("포인트 잔고가 부족합니다. 현재 잔고: %d, 필요 포인트: %d", balance, required));
    }
}
