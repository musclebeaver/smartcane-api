package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.NotBlank;

public record RevokeOfflineTokenRequest(
        @NotBlank String token
) {}
