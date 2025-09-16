package com.smartcane.api.domain.billing.repository;

import com.smartcane.api.domain.billing.entity.WebhookReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookReceiptRepository extends JpaRepository<WebhookReceipt, Long> {
    boolean existsByProviderAndTransmissionId(String provider, String transmissionId);
}
