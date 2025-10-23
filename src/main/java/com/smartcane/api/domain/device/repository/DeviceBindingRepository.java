package com.smartcane.api.domain.device.repository;

import com.smartcane.api.domain.device.entity.DeviceBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceBindingRepository extends JpaRepository<DeviceBinding, UUID> {

    Optional<DeviceBinding> findByDeviceIdAndActive(UUID deviceId, boolean active);

    Optional<DeviceBinding> findByDeviceIdAndUserIdAndActive(UUID deviceId, Long userId, boolean active);

    List<DeviceBinding> findByUserIdAndActive(Long userId, boolean active);

    List<DeviceBinding> findByUserIdOrderByBoundAtDesc(Long userId);
}
