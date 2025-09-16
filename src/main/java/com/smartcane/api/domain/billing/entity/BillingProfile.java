package com.smartcane.api.domain.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "billing_profile", indexes = {
        @Index(name = "idx_billing_user", columnList = "userId"),
        @Index(name = "idx_billing_customerKey", columnList = "customerKey", unique = true)
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BillingProfile {
    public enum BillingStatus { ACTIVE, INACTIVE, REVOKED } //

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;            // 스마트 지팡이 사용자 PK

    @Column(nullable = false, unique = true, length = 64)
    private String customerKey;     // PG 매핑 키

    @Column(length = 128)
    private String billingKey;      // 토스 빌링키

    @Enumerated(EnumType.STRING)
    private BillingStatus status;   // ACTIVE, INACTIVE, REVOKED

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
