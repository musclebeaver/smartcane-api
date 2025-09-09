package com.smartcane.api.domain.device.entity;

import com.smartcane.api.common.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
//지팡이 단말 자체(시리얼, 모델, 펌웨어, 상태, 마지막 접속 시각).
@Entity
@Table(name = "device",
        indexes = {
                @Index(name = "ux_device_serial", columnList = "serial", unique = true),
                @Index(name = "ix_device_status", columnList = "status")
        })
@Getter @Setter
public class Device extends Auditable {

    public enum Status { ACTIVE, INACTIVE, LOST } // 단말 상태

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // PK

    @Column(nullable = false, length = 64, unique = true)
    private String serial;             // 단말 고유 시리얼 번호 (유니크)

    @Column(length = 60)
    private String model;              // 모델명 (예: SmartCane-V1)

    @Column(length = 40)
    private String firmware;           // 펌웨어 버전

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Status status = Status.ACTIVE; // 상태: 사용중/비활성/분실

    private Instant lastSeenAt;        // 마지막 서버 접속 시각
}
