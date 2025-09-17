package com.smartcane.api.domain.billing.dto.fare;

import lombok.Builder;
import java.time.OffsetDateTime;

@Builder
public record RideContext(
        Long rideId,            // 선택: 식별용
        Long userId,
        String deviceId,
        TransportMode mode,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        int distanceM,
        boolean accessibleUser,
        boolean hasSubscription
) {}
