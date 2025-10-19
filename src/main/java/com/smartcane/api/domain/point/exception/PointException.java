package com.smartcane.api.domain.point.exception;

/**
 * 포인트 도메인에서 공통적으로 사용될 사용자 정의 예외의 최상위 타입입니다.
 * 추후 세부 예외를 확장할 수 있도록 런타임 예외를 상속받습니다.
 */
public class PointException extends RuntimeException {

    public PointException(String message) {
        super(message);
    }

    public PointException(String message, Throwable cause) {
        super(message, cause);
    }
}
