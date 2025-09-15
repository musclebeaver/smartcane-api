package com.smartcane.api.domain.identity.dto;

import jakarta.validation.constraints.*;

public record UserLoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
