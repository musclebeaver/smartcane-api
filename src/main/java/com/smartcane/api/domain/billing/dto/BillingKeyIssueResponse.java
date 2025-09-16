package com.smartcane.api.domain.billing.dto;

public record BillingKeyIssueResponse(
        String billingKey,
        String cardCompany,
        String cardNumberMasking
) {}