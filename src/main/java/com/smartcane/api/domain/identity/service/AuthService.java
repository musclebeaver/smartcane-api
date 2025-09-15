package com.smartcane.api.domain.identity.service;

import com.smartcane.api.domain.identity.dto.*;

public interface AuthService {
    /** 로그인: 비밀번호 검증 → access/refresh 발급 + refresh 해시 저장(회전 시작점) */
    AuthResponse login(UserLoginRequest request);

    /** 리프레시: typ=refresh 토큰만 허용, 소유자 확인 후 access/refresh 재발급 + 해시 교체(회전) */
    AuthResponse refresh(TokenRefreshRequest request);

    /** 로그아웃: 현재 사용자와 요청된 refresh 소유자 일치 시 해당 해시 제거(무효화) */
    void logout(Long userId, String refreshToken);
}
