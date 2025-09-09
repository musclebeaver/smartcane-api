package com.smartcane.api.domain.identity.entity;

import com.smartcane.api.common.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


//서버 간 연동/운영도구/파트너용 API 키 발급·권한 스코프 관리.
@Entity
@Table(name = "api_client",
        indexes = {
                @Index(name = "ix_apiclient_keyhash", columnList = "apiKeyHash"),
                @Index(name = "ix_apiclient_enabled", columnList = "enabled")
        })
@Getter @Setter
public class ApiClient extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                       // PK

    @Column(nullable = false, length = 80)
    private String name;                   // 클라이언트 이름 (예: 배치, 관리자 툴)

    @Column(nullable = false, length = 128)
    private String apiKeyHash;             // 발급된 API 키의 해시값

    @Column(columnDefinition = "json")
    private String scopes;                 // 접근 가능한 권한 범위 (JSON 배열)

    @Column(nullable = false)
    private Integer rateLimitPerMin = 60;  // 분당 호출 제한

    @Column(nullable = false)
    private Boolean enabled = true;        // 사용 가능 여부
}
