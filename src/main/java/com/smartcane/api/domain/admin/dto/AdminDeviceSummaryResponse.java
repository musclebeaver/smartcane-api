package com.smartcane.api.domain.admin.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * 관리자 디바이스 목록 응답 스켈레톤.
 */
public record AdminDeviceSummaryResponse(
        UUID id,
        String serialNo,
        String displayName,
        DeviceStatus status,
        Instant createdAt
) {
    /**
     * 관리자 기능에서 사용할 디바이스 상태 표현용 내부 enum.
     * 실제 구현 시 Device 엔티티의 상태 정책과 동기화해야 한다.
     */
    public enum DeviceStatus {
        ACTIVE,
        SUSPENDED,
        RETIRED
    }
}
