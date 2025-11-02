package com.smartcane.api.domain.admin.dto;

import com.smartcane.api.domain.identity.entity.User;
import java.time.Instant;

/**
 * 관리자 전용 사용자 목록 응답 스켈레톤.
 * 실제 구현 시 User 엔티티를 변환하도록 교체하면 된다.
 */
public record AdminUserSummaryResponse(
        Long id,
        String email,
        String nickname,
        User.Role role,
        User.Status status,
        Instant createdAt
) {
}
