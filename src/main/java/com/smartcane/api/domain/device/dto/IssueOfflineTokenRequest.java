package com.smartcane.api.domain.device.dto;

import jakarta.validation.constraints.*;

public record IssueOfflineTokenRequest(
        @NotBlank String scope,
        @Positive @Max(7*24*60*60) long ttlSeconds // 최대 7일
) {}
