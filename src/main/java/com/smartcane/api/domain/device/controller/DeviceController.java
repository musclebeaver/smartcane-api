package com.smartcane.api.domain.device.controller;

import com.smartcane.api.domain.device.dto.*;
import com.smartcane.api.domain.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // 디바이스 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceRegisterResponse register(@Valid @RequestBody DeviceRegisterRequest req) {
        return deviceService.register(req);
    }

    // 단건 조회 (검증용)
    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable("id") UUID id) {
        return deviceService.get(id);
    }
}
