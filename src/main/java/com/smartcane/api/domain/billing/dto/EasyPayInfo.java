package com.smartcane.api.domain.billing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EasyPayInfo(
        String provider,  // 예: TOSS, NAVER, KAKAOPAY
        String amount
) {}
