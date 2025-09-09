package com.smartcane.api.domain.device.entity;

import com.smartcane.api.common.model.Auditable;
import com.smartcane.api.domain.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
//오프라인 토큰 수명주기 추적
@Entity
@Table(name = "offline_token",
        indexes = {
                @Index(name = "ix_offtoken_device", columnList = "device_id"),
                @Index(name = "ix_offtoken_expires", columnList = "expiresAt"),
                @Index(name = "ux_offtoken_jti", columnList = "jti", unique = true)
        })
@Getter @Setter
public class OfflineToken extends Auditable {

    public enum Status { VALID, REVOKED, USED } // 토큰 상태

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // PK

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;                 // 사용자(선택적)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "device_id", nullable = false)
    private Device device;             // 단말

    @Column(length = 64, nullable = false, unique = true)
    private String jti;                // 토큰 식별자 (JWT ID)

    private Instant issuedAt;          // 발급 시각
    private Instant expiresAt;         // 만료 시각

    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private Status status = Status.VALID; // 상태 기본 VALID

    @Column(length = 64)
    private String audience;           // 토큰 대상 (예: "bus-fare")

    @Column(columnDefinition = "json")
    private String scope;              // 권한 범위 (예: {"limit":2500})
}
