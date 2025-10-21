package com.smartcane.api.domain.billing.entity;

import com.smartcane.api.domain.billing.dto.fare.TransportMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "ride_log",
        indexes = {
                @Index(name="idx_ride_user_started",   columnList="user_id,started_at"),
                @Index(name="idx_ride_started",        columnList="started_at"),
                @Index(name="idx_ride_device_started", columnList="device_id,started_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RideLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 식별자 */
    @Column(nullable = false)
    private Long userId;

    /** 디바이스 식별자(스마트 지팡이) */
    @Column(length = 64)
    private String deviceId;

    /** 교통수단: BUS | SUBWAY */
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private TransportMode mode;

    /** 탑승(시작) 시각 */
    @Column(nullable = false)
    private OffsetDateTime startedAt;

    /** 하차(종료) 시각 */
    @Column(nullable = false)
    private OffsetDateTime endedAt;

    /** (옵션) 이동거리(m). 정액 요금 정책이라 0 가능 */
    @Column(name = "distance_m")   // 👈 snake_case로 명시
    private Integer distanceM;

    /** (옵션) 노선/역 정보 — 운영 분석용 */
    @Column(length = 64)  private String routeId;       // 버스 노선ID / 지하철 노선ID
    @Column(length = 128) private String routeName;     // 143번, 2호선 등
    @Column(length = 64)  private String originId;      // 정류장/역 코드
    @Column(length = 128) private String originName;
    @Column(length = 64)  private String destId;
    @Column(length = 128) private String destName;
}
