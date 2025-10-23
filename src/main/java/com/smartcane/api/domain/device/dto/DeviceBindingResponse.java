package com.smartcane.api.domain.device.dto;

import java.time.Instant;
import java.util.UUID;

public record DeviceBindingResponse(
        UUID bindingId,
        UUID deviceId,
        Long userId,
        boolean active,
        Instant boundAt,
        Instant unboundAt
) {}
