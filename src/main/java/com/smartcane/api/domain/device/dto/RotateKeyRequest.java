package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.NotBlank;

public record RotateKeyRequest(
        @NotBlank String algorithm // "ED25519" | "ES256"
) {}
