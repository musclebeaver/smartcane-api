package com.smartcane.api.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
@Getter
public abstract class Auditable {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    protected Instant updatedAt;
}
