package com.smartcane.api.domain.billing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CardInfo(
        String company,        // 예: "현대카드"
        String number,         // "1234-****-****-5678"
        String issuerCode,     // 발급사 코드
        String acquirerCode,   // 매입사 코드
        String ownerType,      // 개인/법인
        String installmentPlanMonths,
        Boolean isInterestFree
) {}
