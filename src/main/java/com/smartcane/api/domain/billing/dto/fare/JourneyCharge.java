package com.smartcane.api.domain.billing.dto.fare;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JourneyCharge(
        Long userId,
        String orderId,             // 이 여정의 청구용 orderId
        OffsetDateTime startedAt,   // 여정 시작
        OffsetDateTime endedAt,     // 여정 종료
        List<RideContext> rides,    // 묶인 탑승들
        FareBreakdown breakdown     // 최종 요금(환승 정책 적용 후)
) {}
