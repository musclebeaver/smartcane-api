package com.smartcane.api.domain.admin.dto;

import com.smartcane.api.domain.identity.entity.User;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 관리자 화면에서 단일 사용자 상세 정보를 내려줄 때 사용할 DTO 스켈레톤.
 */
public record AdminUserDetailResponse(
        Long id,
        String email,
        String nickname,
        LocalDate birthDate,
        User.Role role,
        User.Status status,
        Instant createdAt,
        Instant lastLoginAt
) {
}
