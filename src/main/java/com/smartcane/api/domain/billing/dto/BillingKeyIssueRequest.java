package com.smartcane.api.domain.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BillingKeyIssueRequest(
        @NotNull Long userId,
        @NotBlank String customerKey,
        @NotBlank String authKeyOrToken,
        String cardAlias
) {}