package com.smartcane.api.domain.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name="webhook_receipt",
        uniqueConstraints=@UniqueConstraint(name="uk_provider_transmission", columnNames={"provider","transmission_id"}),
        indexes = { @Index(name="idx_wr_received_at", columnList="received_at") }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookReceipt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String provider;          // "TOSS"

    @Column(nullable = false, length = 128)
    private String transmissionId;    // tosspayments-webhook-transmission-id (없으면 fallback 키)

    @Column(length = 64)
    private String eventType;         // payment.approved, payment.canceled, billingKey.deleted...

    @Column(nullable = false)
    private OffsetDateTime receivedAt;
}
