package com.smartcane.api.domain.billing.dto.fare;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FareBreakdown(
        String currency,
        BigDecimal subtotal,          // 가산/할인 전 합계 (기본+거리+시간)
        BigDecimal surcharges,        // 피크/야간 등 가산 총액
        BigDecimal discounts,         // (-) 할인 총액
        BigDecimal total,             // 최종 청구금액 (반올림 적용 후)
        List<FareItem> items          // 세부내역
) {}
