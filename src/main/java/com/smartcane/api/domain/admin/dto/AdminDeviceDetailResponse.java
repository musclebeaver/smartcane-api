package com.smartcane.api.domain.admin.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * 관리자 디바이스 상세 응답 스켈레톤.
 */
public record AdminDeviceDetailResponse(
        UUID id,
        String serialNo,
        String displayName,
        AdminDeviceSummaryResponse.DeviceStatus status,
        Instant createdAt,
        Instant updatedAt,
        Long ownerId
) {
}
