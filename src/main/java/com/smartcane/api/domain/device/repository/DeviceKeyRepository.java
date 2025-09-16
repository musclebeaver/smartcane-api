package com.smartcane.api.domain.device.repository;

import com.smartcane.api.domain.device.entity.DeviceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

import java.util.UUID;

public interface DeviceKeyRepository extends JpaRepository<DeviceKey, UUID> {
    Optional<DeviceKey> findByDeviceIdAndActive(UUID deviceId, boolean active);
    Optional<DeviceKey> findByKid(String kid);
    List<DeviceKey> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);
}
