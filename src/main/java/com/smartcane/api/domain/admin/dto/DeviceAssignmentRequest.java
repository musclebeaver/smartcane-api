package com.smartcane.api.domain.admin.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 관리자 디바이스 소유자 지정/변경 요청 스켈레톤.
 */
public record DeviceAssignmentRequest(
        @NotNull(message = "연결할 회원 ID는 필수입니다.") Long userId
) {
}
