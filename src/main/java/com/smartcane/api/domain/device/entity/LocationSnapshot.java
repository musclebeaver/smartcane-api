package com.smartcane.api.domain.device.entity;

import com.smartcane.api.common.model.Auditable;
import com.smartcane.api.domain.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
//단말(또는 사용자)의 시점별 위치 로그
@Entity
@Table(name = "location_snapshot",
        indexes = {
                @Index(name = "ix_locsnap_device", columnList = "device_id"),
                @Index(name = "ix_locsnap_capturedAt", columnList = "capturedAt"),
                @Index(name = "ix_locsnap_latlng", columnList = "lat,lng")
        })
@Getter @Setter
public class LocationSnapshot extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // PK

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;                 // 사용자 (없을 수 있음)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "device_id", nullable = false)
    private Device device;             // 단말

    @Column(nullable = false, precision = 9, scale = 6)
    private Double lat;                // 위도

    @Column(nullable = false, precision = 9, scale = 6)
    private Double lng;                // 경도

    @Column(precision = 6, scale = 2)
    private Double accuracyM;          // 측위 정확도 (미터)

    @Column(nullable = false)
    private Instant capturedAt;        // 수집된 시각
}
