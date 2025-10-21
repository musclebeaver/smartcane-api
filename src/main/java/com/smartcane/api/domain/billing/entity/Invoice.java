package com.smartcane.api.domain.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

//청구서
@Entity
@Table(name="invoice", indexes = {
        @Index(name="uk_invoice_order_id", columnList="order_id", unique = true),
        @Index(name="idx_invoice_user",    columnList="user_id"),
        @Index(name="idx_invoice_status",  columnList="status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    public enum Status { PENDING, PAYING, PAID, FAILED, CANCELED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    /** 결제 멱등키(자동결제의 orderId와 동일) */
    @Column(nullable = false, length = 120, unique = true)
    private String orderId;

    /** 가독용 제목: 예) 교통요금(2025-09-17 08:10 ~ 09:20) */
    @Column(length = 200)
    private String title;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 8, nullable = false)
    private String currency; // KRW

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Status status; // PENDING → PAYING → PAID / FAILED / CANCELED

    /** 결제 식별자(토스) */
    @Column(length = 64)
    private String paymentKey;

    private OffsetDateTime billedFrom;  // 여정 시작
    private OffsetDateTime billedTo;    // 여정 종료

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate  void onUpdate(){ updatedAt = OffsetDateTime.now(); }
}
