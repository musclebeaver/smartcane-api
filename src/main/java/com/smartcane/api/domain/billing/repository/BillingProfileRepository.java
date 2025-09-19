package com.smartcane.api.domain.billing.repository;

import com.smartcane.api.domain.billing.entity.BillingProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingProfileRepository extends JpaRepository<BillingProfile, Long> {
    Optional<BillingProfile> findByUserId(Long userId);
    Optional<BillingProfile> findByCustomerKey(String customerKey);


    Optional<BillingProfile> findFirstByUserIdAndStatusOrderByUpdatedAtDesc(
            Long userId, BillingProfile.BillingStatus status
    );
}