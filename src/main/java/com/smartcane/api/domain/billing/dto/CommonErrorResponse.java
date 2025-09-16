package com.smartcane.api.domain.billing.dto;

public record CommonErrorResponse(
        String code,
        String message
) {}