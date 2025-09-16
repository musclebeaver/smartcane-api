package com.smartcane.api.domain.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CancelRequest(
        @NotNull Long userId,
        @NotBlank String paymentKey,
        @NotBlank String cancelReason
) {}