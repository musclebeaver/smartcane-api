package com.smartcane.api.domain.identity.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}
