package com.smartcane.api.domain.identity.controller;

import com.smartcane.api.domain.identity.dto.*;
import com.smartcane.api.domain.identity.service.AuthService;
import com.smartcane.api.domain.identity.service.UserService;
import com.smartcane.api.security.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody UserLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        // 리프레시 토큰(typ=refresh)만 허용하도록 AuthService에서 검증
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        Long userId = AuthUtil.currentUserId(); // 현재 인증된 사용자
        authService.logout(userId, request.refreshToken());
    }
}
