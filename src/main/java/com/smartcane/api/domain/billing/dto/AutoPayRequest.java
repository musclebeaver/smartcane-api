package com.smartcane.api.domain.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AutoPayRequest(
        @NotNull Long userId,
        @NotBlank String customerKey,
        @NotBlank String billingKey,
        @NotBlank String orderId,
        @NotBlank String orderName,
        @NotNull BigDecimal amount,
        String currency
) {}