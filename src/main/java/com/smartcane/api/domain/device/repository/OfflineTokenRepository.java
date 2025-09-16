package com.smartcane.api.domain.device.repository;

import com.smartcane.api.domain.device.entity.OfflineToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.*;

import java.util.UUID;

public interface OfflineTokenRepository extends JpaRepository<OfflineToken, UUID> {
    List<OfflineToken> findByDeviceIdAndRevokedFalse(UUID deviceId);
    Optional<OfflineToken> findByDeviceIdAndToken(UUID deviceId, String token);
    List<OfflineToken> findByExpiresAtBefore(Instant instant);
}
