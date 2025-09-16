package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyOfflineTokenRequest(
        @NotBlank String token
) {}
