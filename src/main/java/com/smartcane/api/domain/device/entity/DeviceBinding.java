package com.smartcane.api.domain.device.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "device_binding", indexes = {
        @Index(name = "ix_binding_user", columnList = "user_id"),
        @Index(name = "ix_binding_device_active", columnList = "device_id,active")
})
public class DeviceBinding {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "active", nullable = false)
    private boolean active;  // true=현재 바인딩, false=해지됨

    @Column(name = "bound_at", nullable = false)
    private Instant boundAt;

    @Column(name = "unbound_at")
    private Instant unboundAt;

    @PrePersist
    void prePersist() {
        this.boundAt = (this.boundAt == null) ? Instant.now() : this.boundAt;
        // 기본 신규 바인딩은 active=true
        if (!this.active) this.active = true;
    }
}
