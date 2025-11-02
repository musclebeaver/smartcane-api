package com.smartcane.api.domain.admin.service;

import com.smartcane.api.domain.admin.dto.AdminUserDetailResponse;
import com.smartcane.api.domain.admin.dto.AdminUserSummaryResponse;
import com.smartcane.api.domain.admin.dto.AdminUserUpdateRequest;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 관리자 회원 관리 기능 스켈레톤 서비스.
 * 실제 구현 시 repository, mapper 등을 주입받아 비즈니스 로직을 완성하면 된다.
 */
@Slf4j
@Service
public class AdminUserService {

    public List<AdminUserSummaryResponse> listUsers() {
        // TODO: 관리자 회원 조회 로직 구현 (페이징, 검색 등 확장 가능)
        log.debug("[ADMIN] 사용자 목록 조회 스켈레톤 호출");
        return Collections.emptyList();
    }

    public AdminUserDetailResponse getUser(Long userId) {
        // TODO: 단일 사용자 상세 조회 로직 구현
        throw new UnsupportedOperationException("관리자 사용자 상세 조회 로직이 아직 구현되지 않았습니다. userId=" + userId);
    }

    public AdminUserDetailResponse updateUser(Long userId, AdminUserUpdateRequest request) {
        // TODO: 권한/상태 변경 등 사용자 수정 로직 구현
        throw new UnsupportedOperationException("관리자 사용자 수정 로직이 아직 구현되지 않았습니다. userId=" + userId);
    }
}
