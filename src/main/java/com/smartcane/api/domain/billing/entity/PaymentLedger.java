package com.smartcane.api.domain.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name="payment_ledger", indexes = {
        @Index(name="idx_pl_payment_key", columnList="payment_key"),
        @Index(name="idx_pl_order_id",    columnList="order_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentLedger {

    public enum Status { READY, IN_PROGRESS, DONE, PARTIAL_CANCELED, CANCELED, ABORTED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String paymentKey;

    @Column(nullable = false, length = 100)
    private String orderId;

    @Column(length = 200)
    private String orderName;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 8, nullable = false)
    private String currency; // KRW

    @Column(length = 16)
    private String method;   // CARD, VIRTUAL_ACCOUNT, EASY_PAY ...

    @Column(length = 8)
    private String issuerCode;

    @Column(length = 8)
    private String acquirerCode;

    @Enumerated(EnumType.STRING)
    private Status status;

    private OffsetDateTime approvedAt;
    private OffsetDateTime canceledAt;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
