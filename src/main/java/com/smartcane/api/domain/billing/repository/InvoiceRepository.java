package com.smartcane.api.domain.billing.repository;

import com.smartcane.api.domain.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrderId(String orderId);
    List<Invoice> findByStatus(Invoice.Status status);
    List<Invoice> findByUserIdAndCreatedAtBetween(Long userId, OffsetDateTime from, OffsetDateTime to);
}
