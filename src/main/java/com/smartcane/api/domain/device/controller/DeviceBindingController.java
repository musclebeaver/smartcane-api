package com.smartcane.api.domain.device.controller;

import com.smartcane.api.domain.device.dto.*;
import com.smartcane.api.domain.device.service.DeviceBindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceBindingController {

    private final DeviceBindingService bindingService;

    // 기기 바인딩 생성 (한 기기당 active 1개 유지)
    @PostMapping("/api/devices/{deviceId}/bind")
    @ResponseStatus(HttpStatus.CREATED)
    public BindDeviceResponse bind(@PathVariable UUID deviceId,
                                   @Valid @RequestBody BindDeviceRequest req) {
        return bindingService.bind(deviceId, req);
    }

    // 기기 바인딩 해지 (해당 사용자 기준)
    @PostMapping("/api/devices/{deviceId}/unbind")
    public UnbindDeviceResponse unbind(@PathVariable UUID deviceId,
                                       @Valid @RequestBody UnbindDeviceRequest req) {
        return bindingService.unbind(deviceId, req);
    }

    // 기기의 현재 active 바인딩 조회
    @GetMapping("/api/devices/{deviceId}/binding")
    public DeviceBindingResponse getActiveBinding(@PathVariable UUID deviceId) {
        return bindingService.getActiveBindingByDevice(deviceId);
    }

    // 사용자 기준 바인딩 목록 조회 (active 필터 가능: true/false/null)
    @GetMapping("/api/users/{userId}/device-bindings")
    public List<DeviceBindingResponse> listByUser(@PathVariable UUID userId,
                                                  @RequestParam(required = false) Boolean active) {
        return bindingService.getBindingsByUser(userId, active);
    }
}
