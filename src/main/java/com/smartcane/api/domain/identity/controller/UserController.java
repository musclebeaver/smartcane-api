package com.smartcane.api.domain.identity.controller;

import com.smartcane.api.domain.identity.dto.UserResponse;
import com.smartcane.api.domain.identity.dto.UserUpdateRequest;
import com.smartcane.api.domain.identity.service.UserService;
import com.smartcane.api.security.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // SecurityContext에서 userId 추출
    @GetMapping("/me")
    public UserResponse me() {
        Long userId = AuthUtil.currentUserId();
        return userService.getMe(userId);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UserUpdateRequest request) {
        Long userId = AuthUtil.currentUserId();
        return userService.updateProfile(userId, request);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN만 조회 허용
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
