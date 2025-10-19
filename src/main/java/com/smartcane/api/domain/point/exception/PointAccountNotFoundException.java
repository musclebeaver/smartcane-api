package com.smartcane.api.domain.point.exception;

/**
 * 사용자의 포인트 계정이 존재하지 않을 때 발생하는 예외입니다.
 */
public class PointAccountNotFoundException extends PointException {

    public PointAccountNotFoundException(Long userId) {
        super(String.format("사용자(%d)의 포인트 계정을 찾을 수 없습니다.", userId));
    }
}
