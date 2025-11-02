package com.smartcane.api.domain.admin.dto;

import com.smartcane.api.domain.identity.entity.User;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자 전용 사용자 정보 수정 요청 스켈레톤.
 */
public record AdminUserUpdateRequest(
        @NotNull(message = "변경할 권한은 필수입니다.") User.Role role,
        @NotNull(message = "변경할 상태는 필수입니다.") User.Status status
) {
}
