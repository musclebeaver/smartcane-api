package com.smartcane.api.domain.identity.dto;

import com.smartcane.api.domain.identity.entity.User;
import java.time.Instant;
import java.time.LocalDate;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        LocalDate birthDate,
        User.Role role,
        User.Status status,
        Instant createdAt
) {}
