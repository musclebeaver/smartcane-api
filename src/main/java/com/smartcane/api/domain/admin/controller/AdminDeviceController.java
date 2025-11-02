package com.smartcane.api.domain.admin.controller;

import com.smartcane.api.domain.admin.dto.AdminDeviceDetailResponse;
import com.smartcane.api.domain.admin.dto.AdminDeviceSummaryResponse;
import com.smartcane.api.domain.admin.dto.DeviceAssignmentRequest;
import com.smartcane.api.domain.admin.service.AdminDeviceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 디바이스 관리 엔드포인트 스켈레톤.
 */
@RestController
@RequestMapping("/api/admin/devices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeviceController {

    private final AdminDeviceService adminDeviceService;

    @GetMapping
    public List<AdminDeviceSummaryResponse> listDevices() {
        // TODO: 필터 조건(상태, 등록일 등)을 받아서 처리하도록 확장
        return adminDeviceService.listDevices();
    }

    @GetMapping("/{deviceId}")
    public AdminDeviceDetailResponse getDevice(@PathVariable UUID deviceId) {
        // TODO: 존재하지 않는 경우 예외 처리 추가
        return adminDeviceService.getDevice(deviceId);
    }

    @PostMapping("/{deviceId}/assign")
    public void assignDevice(@PathVariable UUID deviceId,
                             @Valid @RequestBody DeviceAssignmentRequest request) {
        // TODO: 디바이스 사용 이력 저장 등의 부가 로직 고려
        adminDeviceService.assignDevice(deviceId, request);
    }

    @DeleteMapping("/{deviceId}/assign")
    public void releaseDevice(@PathVariable UUID deviceId) {
        // TODO: 해제 이벤트 발행 등 추가 요구사항을 구현
        adminDeviceService.releaseDevice(deviceId);
    }
}
