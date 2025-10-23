package com.smartcane.api.domain.device.dto;

import java.time.Instant;
import java.util.UUID;

public record UnbindDeviceResponse(
        UUID bindingId,
        UUID deviceId,
        Long userId,
        boolean active,
        Instant boundAt,
        Instant unboundAt
) {}
