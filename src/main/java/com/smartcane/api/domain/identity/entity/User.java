package com.smartcane.api.domain.identity.entity;

import com.smartcane.api.common.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

//회원 기본 프로필과 권한
@Entity
@Table(name = "users",
        indexes = { @Index(name = "ux_users_email", columnList = "email", unique = true) })
@Getter @Setter
public class User extends Auditable {

    public enum Role { USER, ADMIN,SYSTEM,CS }       // 권한: 일반 사용자 / 관리자
    public enum Status { ACTIVE, SUSPENDED, DELETED } // 계정 상태

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // PK: 내부 식별자

    @Column(nullable = false, length = 190, unique = true)
    private String email;                   // 로그인용 이메일 (유니크)

    @Column(length = 60)
    private String nickname;                // 표시 이름

    @Column(columnDefinition = "DATE")
    private LocalDate birthDate;   // 생년월일 (선택적)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role = Role.USER;          // 권한 (기본 USER)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.ACTIVE;  // 계정 상태 (기본 ACTIVE)
}

