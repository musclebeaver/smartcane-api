package com.smartcane.api.domain.device.service;

import com.smartcane.api.domain.device.dto.*;
import com.smartcane.api.domain.device.entity.Device;
import com.smartcane.api.domain.device.entity.DeviceBinding;
import com.smartcane.api.domain.device.repository.DeviceBindingRepository;
import com.smartcane.api.domain.device.repository.DeviceRepository;
import com.smartcane.api.domain.device.support.DuplicateResourceException;
import com.smartcane.api.domain.device.support.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceBindingService {

    private final DeviceRepository deviceRepository;
    private final DeviceBindingRepository bindingRepository;

    /**
     * 한 기기에는 동시에 1개의 active 바인딩만 허용.
     * 기존 active가 있으면 해지 후 새 바인딩 생성(또는 같은 유저면 중복 예외 처리).
     */
    @Transactional
    public BindDeviceResponse bind(UUID deviceId, BindDeviceRequest req) {
        // 기기 존재 확인
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("디바이스를 찾을 수 없습니다: " + deviceId));

        // 이미 같은 유저로 active 바인딩이 있으면 중복
        bindingRepository.findByDeviceIdAndUserIdAndActive(device.getId(), req.userId(), true)
                .ifPresent(b -> {
                    throw new DuplicateResourceException("이미 해당 사용자로 바인딩되어 있습니다.");
                });

        // 기존 active 바인딩 있으면 해지
        bindingRepository.findByDeviceIdAndActive(device.getId(), true)
                .ifPresent(prev -> {
                    prev.setActive(false);
                    prev.setUnboundAt(Instant.now());
                });

        // 새 바인딩
        DeviceBinding created = bindingRepository.save(DeviceBinding.builder()
                .deviceId(device.getId())
                .userId(req.userId())
                .active(true)
                .build());

        return new BindDeviceResponse(
                created.getId(),
                created.getDeviceId(),
                created.getUserId(),
                created.isActive(),
                created.getBoundAt(),
                created.getUnboundAt()
        );
    }

    /**
     * 특정 사용자-기기의 active 바인딩 해지
     */
    @Transactional
    public UnbindDeviceResponse unbind(UUID deviceId, UnbindDeviceRequest req) {
        DeviceBinding binding = bindingRepository
                .findByDeviceIdAndUserIdAndActive(deviceId, req.userId(), true)
                .orElseThrow(() -> new NotFoundException("활성 바인딩이 없습니다."));

        binding.setActive(false);
        binding.setUnboundAt(Instant.now());

        return new UnbindDeviceResponse(
                binding.getId(),
                binding.getDeviceId(),
                binding.getUserId(),
                binding.isActive(),
                binding.getBoundAt(),
                binding.getUnboundAt()
        );
    }

    /**
     * 기기의 현재 active 바인딩 조회(없으면 404)
     */
    @Transactional
    public DeviceBindingResponse getActiveBindingByDevice(UUID deviceId) {
        DeviceBinding b = bindingRepository.findByDeviceIdAndActive(deviceId, true)
                .orElseThrow(() -> new NotFoundException("활성 바인딩이 없습니다."));
        return new DeviceBindingResponse(
                b.getId(), b.getDeviceId(), b.getUserId(), b.isActive(), b.getBoundAt(), b.getUnboundAt()
        );
    }

    /**
     * 사용자 기준 바인딩 목록(필터: active)
     */
    @Transactional
    public List<DeviceBindingResponse> getBindingsByUser(Long userId, Boolean active) {
        List<DeviceBinding> list = (active == null)
                ? bindingRepository.findByUserIdOrderByBoundAtDesc(userId)
                : bindingRepository.findByUserIdAndActive(userId, active);

        return list.stream()
                .map(b -> new DeviceBindingResponse(
                        b.getId(), b.getDeviceId(), b.getUserId(), b.isActive(), b.getBoundAt(), b.getUnboundAt()
                ))
                .toList();
    }
}
