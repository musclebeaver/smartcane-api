package com.smartcane.api.domain.billing.dto.fare;

import java.math.BigDecimal;

public record FareItem(
        String type,           // BASE, DISTANCE, TIME, PEAK, NIGHT, DISCOUNT_ACCESSIBLE, DISCOUNT_SUBSCRIPTION, ROUNDING
        String description,    // 설명
        BigDecimal amount      // + 가산, - 할인
) {}
