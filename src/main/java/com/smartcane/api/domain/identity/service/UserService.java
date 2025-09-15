package com.smartcane.api.domain.identity.service;

import com.smartcane.api.domain.identity.dto.UserResponse;
import com.smartcane.api.domain.identity.dto.UserSignupRequest;
import com.smartcane.api.domain.identity.dto.UserUpdateRequest;

public interface UserService {
    UserResponse signup(UserSignupRequest request);       // 회원가입
    UserResponse getMe(Long userId);                      // 내 정보
    UserResponse getById(Long id);                        // (관리자/내부) 단건조회
    UserResponse updateProfile(Long userId, UserUpdateRequest request);
}
