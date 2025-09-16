package com.smartcane.api.domain.device.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "device_key", indexes = {
        @Index(name = "ix_key_device", columnList = "device_id"),
        @Index(name = "ux_key_kid", columnList = "kid", unique = true)
})
public class DeviceKey {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "kid", nullable = false, length = 64)
    private String kid;

    // ⚠️ 데모: 평문 저장. 운영: 반드시 KMS/암호화(AES-GCM 등)로 보호!
    @Lob @Column(name = "jwk_private_json", nullable = false)
    private String jwkPrivateJson;

    @Lob @Column(name = "jwk_public_json", nullable = false)
    private String jwkPublicJson;

    @Column(name = "algorithm", nullable = false, length = 16) // ED25519 / ES256
    private String algorithm;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
