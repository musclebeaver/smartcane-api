package com.smartcane.api.domain.device.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;


import java.time.Instant;
import java.util.UUID;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "device", indexes = {
        @Index(name = "ux_device_sn", columnList = "serial_no", unique = true),
        @Index(name = "ix_device_status", columnList = "status")
})
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Comment("제조 시리얼")
    @Column(name = "serial_no", nullable = false, unique = true, length = 64)
    private String serialNo;


    @Comment("디바이스 표시명")
    @Column(name = "display_name", length = 128)
    private String displayName;


    @Comment("활성/정지/폐기")
    @Column(name = "status", nullable = false, length = 16)
    private String status; // ACTIVE, SUSPENDED, RETIRED


    @Comment("등록 시각")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    @Comment("수정 시각")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @PrePersist void prePersist(){
        this.createdAt = this.createdAt == null ? Instant.now() : this.createdAt;
        this.updatedAt = Instant.now();
        this.status = this.status == null ? "ACTIVE" : this.status;
    }
    @PreUpdate void preUpdate(){ this.updatedAt = Instant.now(); }
}