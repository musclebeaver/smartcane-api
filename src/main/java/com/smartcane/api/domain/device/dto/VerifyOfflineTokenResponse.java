package com.smartcane.api.domain.device.dto;

public record VerifyOfflineTokenResponse(
        boolean valid,
        String reason
) {}
