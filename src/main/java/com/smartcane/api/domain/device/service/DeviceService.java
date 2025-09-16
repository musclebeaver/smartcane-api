package com.smartcane.api.domain.device.service;

import com.smartcane.api.domain.device.dto.*;
import com.smartcane.api.domain.device.entity.Device;
import com.smartcane.api.domain.device.repository.DeviceRepository;
import com.smartcane.api.domain.device.support.DuplicateResourceException;
import com.smartcane.api.domain.device.support.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceRegisterResponse register(DeviceRegisterRequest req) {
        deviceRepository.findBySerialNo(req.serialNo()).ifPresent(d -> {
            throw new DuplicateResourceException("이미 등록된 시리얼입니다: " + req.serialNo());
        });

        Device saved = deviceRepository.save(Device.builder()
                .serialNo(req.serialNo())
                .displayName(req.displayName())
                .status("ACTIVE")
                .build());

        return new DeviceRegisterResponse(saved.getId(), saved.getSerialNo(), saved.getStatus());
    }

    @Transactional
    public DeviceResponse get(UUID deviceId) {
        Device d = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("디바이스를 찾을 수 없습니다: " + deviceId));

        return new DeviceResponse(
                d.getId(),
                d.getSerialNo(),
                d.getDisplayName(),
                d.getStatus(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
