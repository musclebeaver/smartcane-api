package com.smartcane.api.domain.admin.controller;

import com.smartcane.api.domain.admin.dto.AdminUserDetailResponse;
import com.smartcane.api.domain.admin.dto.AdminUserSummaryResponse;
import com.smartcane.api.domain.admin.dto.AdminUserUpdateRequest;
import com.smartcane.api.domain.admin.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 회원 관리 엔드포인트 스켈레톤.
 * 모든 요청은 /api/admin/users 로 진입하며, ADMIN 권한 사용자만 접근할 수 있도록 구성한다.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserSummaryResponse> listUsers() {
        // TODO: 페이징/검색 파라미터 확장을 고려하여 구현
        return adminUserService.listUsers();
    }

    @GetMapping("/{userId}")
    public AdminUserDetailResponse getUser(@PathVariable Long userId) {
        // TODO: 에러 핸들링 정책(404 등) 적용
        return adminUserService.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public AdminUserDetailResponse updateUser(@PathVariable Long userId,
                                              @Valid @RequestBody AdminUserUpdateRequest request) {
        // TODO: 감사 로그 등 보안 관련 처리를 추가할 수 있음
        return adminUserService.updateUser(userId, request);
    }
}
