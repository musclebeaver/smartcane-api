package com.smartcane.api.domain.device.dto;


import java.time.Instant;
import java.util.UUID;


public record DeviceResponse(
        UUID deviceId,
        String serialNo,
        String displayName,
        String status,
        Instant createdAt,
        Instant updatedAt
){}
