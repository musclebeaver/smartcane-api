package com.smartcane.api.domain.device.repository;

import com.smartcane.api.domain.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.*;
import java.util.UUID;


public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findBySerialNo(String serialNo);
}