package com.smartcane.api.domain.device.entity;

import com.smartcane.api.common.model.Auditable;
import com.smartcane.api.domain.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
//사용자–단말 연결 기록. 1명이 여러 단말, 단말을 다른 사용자에게 이전하는 경우 추적
@Entity
@Table(name = "device_binding",
        indexes = {
                @Index(name = "ix_binding_user", columnList = "user_id"),
                @Index(name = "ix_binding_device", columnList = "device_id"),
                @Index(name = "ix_binding_active", columnList = "active")
        })
@Getter @Setter
public class DeviceBinding extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // PK

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;                 // 사용자

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "device_id", nullable = false)
    private Device device;             // 단말

    private Instant boundAt;           // 바인딩(연결)된 시각

    @Column(nullable = false)
    private boolean active = true;     // 활성 바인딩 여부
}
