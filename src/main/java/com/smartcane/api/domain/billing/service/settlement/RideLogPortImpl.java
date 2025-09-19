// package: com.smartcane.api.domain.billing.service.settlement.impl

package com.smartcane.api.domain.billing.service.settlement;

import com.smartcane.api.domain.billing.dto.fare.RideContext;
import com.smartcane.api.domain.billing.dto.fare.TransportMode;
import com.smartcane.api.domain.billing.entity.BillingProfile;
import com.smartcane.api.domain.billing.entity.RideLog;
import com.smartcane.api.domain.billing.repository.BillingProfileRepository;
import com.smartcane.api.domain.billing.repository.RideLogRepository;
import com.smartcane.api.domain.billing.service.settlement.RideLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RideLogPortImpl implements RideLogPort {

    private final RideLogRepository rideLogRepository;
    private final BillingProfileRepository billingProfileRepository;

    @Override
    public List<RideContext> findRides(OffsetDateTime from, OffsetDateTime to) {
        List<RideLog> logs = rideLogRepository
                .findByStartedAtBetweenOrderByUserIdAscStartedAtAsc(from, to);

        // RideLog -> RideContext 매핑
        return logs.stream()
                .map(l -> RideContext.builder()
                        .rideId(l.getId())
                        .userId(l.getUserId())
                        .deviceId(l.getDeviceId())
                        .mode(l.getMode() == null ? TransportMode.BUS : l.getMode())
                        .startedAt(l.getStartedAt())
                        .endedAt(l.getEndedAt())
                        .distanceM(l.getDistanceM() == null ? 0 : l.getDistanceM())
                        .accessibleUser(false)   // TODO: 사용자 속성 연동 시 채우기
                        .hasSubscription(false)  // TODO: 구독 연동 시 채우기
                        .build())
                .toList();
    }

    @Override
    public String findCustomerKeyByUserId(Long userId) {
        BillingProfile p = billingProfileRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDesc(
                        userId, BillingProfile.BillingStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalStateException("활성 BillingProfile이 없습니다. userId=" + userId));
        return p.getCustomerKey();
    }

    @Override
    public String findBillingKeyByUserId(Long userId) {
        BillingProfile p = billingProfileRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDesc(
                        userId, BillingProfile.BillingStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalStateException("활성 BillingProfile이 없습니다. userId=" + userId));
        if (p.getBillingKey() == null || p.getBillingKey().isBlank()) {
            throw new IllegalStateException("빌링키가 발급되지 않았습니다. userId=" + userId);
        }
        return p.getBillingKey();
    }
}
