package com.smartcane.api.domain.admin.service;

import com.smartcane.api.domain.admin.dto.AdminDeviceDetailResponse;
import com.smartcane.api.domain.admin.dto.AdminDeviceSummaryResponse;
import com.smartcane.api.domain.admin.dto.DeviceAssignmentRequest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 관리자 디바이스 관리 기능 스켈레톤 서비스.
 */
@Slf4j
@Service
public class AdminDeviceService {

    public List<AdminDeviceSummaryResponse> listDevices() {
        // TODO: 디바이스 목록 조회 (필터링, 정렬 등)
        log.debug("[ADMIN] 디바이스 목록 조회 스켈레톤 호출");
        return Collections.emptyList();
    }

    public AdminDeviceDetailResponse getDevice(UUID deviceId) {
        // TODO: 단일 디바이스 상세 조회 로직 구현
        throw new UnsupportedOperationException("관리자 디바이스 상세 조회 로직이 아직 구현되지 않았습니다. deviceId=" + deviceId);
    }

    public void assignDevice(UUID deviceId, DeviceAssignmentRequest request) {
        // TODO: 특정 사용자에게 디바이스 할당 로직 구현
        throw new UnsupportedOperationException("디바이스 소유자 지정 로직이 아직 구현되지 않았습니다. deviceId=" + deviceId);
    }

    public void releaseDevice(UUID deviceId) {
        // TODO: 디바이스 소유자 해제 로직 구현
        throw new UnsupportedOperationException("디바이스 소유자 해제 로직이 아직 구현되지 않았습니다. deviceId=" + deviceId);
    }
}
