package com.smartcane.api.domain.device.entity;

import com.smartcane.api.common.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
//단말의 공개키/JWK와 KID 관리(키 롤테이션, 서명 검증 신뢰 루트).
@Entity
@Table(name = "device_key",
        indexes = {
                @Index(name = "ix_devicekey_device", columnList = "device_id"),
                @Index(name = "ix_devicekey_kid", columnList = "kid")
        })
@Getter @Setter
public class DeviceKey extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // PK

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "device_id", nullable = false)
    private Device device;             // 단말

    @Column(length = 64, nullable = false)
    private String kid;                // 키 식별자 (Key ID)

    @Column(columnDefinition = "json", nullable = false)
    private String publicJwk;          // 공개키(JWK 형식)

    private Instant rotatedAt;         // 키 회전된 시각
}