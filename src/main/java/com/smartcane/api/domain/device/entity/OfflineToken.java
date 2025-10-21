package com.smartcane.api.domain.device.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "offline_token", indexes = {
        @Index(name = "ix_offline_device", columnList = "device_id"),
        @Index(name = "ix_offline_exp", columnList = "expires_at")
})
public class OfflineToken {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Lob
    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "scope", length = 64)
    private String scope;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @PrePersist void prePersist() {
        if (issuedAt == null) issuedAt = Instant.now();
        if (!revoked) revoked = false;
    }
}
