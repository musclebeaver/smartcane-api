package com.smartcane.api.domain.billing.repository;

import com.smartcane.api.domain.billing.entity.PaymentLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentLedgerRepository extends JpaRepository<PaymentLedger, Long> {
    Optional<PaymentLedger> findByOrderId(String orderId);
    Optional<PaymentLedger> findByPaymentKey(String paymentKey);
}
