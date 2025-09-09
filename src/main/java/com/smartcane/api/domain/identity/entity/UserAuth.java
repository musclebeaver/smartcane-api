package com.smartcane.api.domain.identity.entity;

import com.smartcane.api.common.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
//“인증 수단” 보관. 로컬 비밀번호 해시, 소셜(애플/카카오) 계정 연결, 리프레시 토큰 해시.
@Entity
@Table(name = "user_auth",
        indexes = {
                @Index(name = "ix_userauth_user", columnList = "user_id"),
                @Index(name = "ix_userauth_provider", columnList = "provider"),
                @Index(name = "ix_userauth_refresh_hash", columnList = "refreshTokenHash")
        })
@Getter @Setter
public class UserAuth extends Auditable {

    public enum Provider { LOCAL, APPLE, KAKAO, NAVER } // 인증 제공자

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // PK

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;                         // 소속 사용자

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Provider provider = Provider.LOCAL; // 인증 제공자 타입

    @Column(length = 100)
    private String passwordHash;               // 비밀번호 해시(LOCAL일 때만 사용)

    @Column(length = 128)
    private String refreshTokenHash;           // 리프레시 토큰 해시 (보안상 해시로 저장)

    private Instant revokedAt;                 // 무효화된 시각 (로그아웃 등)
}

