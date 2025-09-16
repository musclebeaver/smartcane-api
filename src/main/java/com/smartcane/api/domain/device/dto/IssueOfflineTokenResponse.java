package com.smartcane.api.domain.device.dto;

import java.time.Instant;

public record IssueOfflineTokenResponse(
        String token,
        Instant expiresAt
) {}
