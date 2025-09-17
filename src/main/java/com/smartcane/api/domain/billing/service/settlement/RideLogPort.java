package com.smartcane.api.domain.billing.service.settlement;

import com.smartcane.api.domain.billing.dto.fare.RideContext;

import java.time.OffsetDateTime;
import java.util.List;

public interface RideLogPort {
    /** 기간 내 사용자별 탑승 기록 불러오기 */
    List<RideContext> findRides(OffsetDateTime from, OffsetDateTime to);
    /** 사용자 → customerKey/billingKey 조회 (이미 구현돼 있다면 그 서비스/Repo 호출) */
    String findCustomerKeyByUserId(Long userId);
    String findBillingKeyByUserId(Long userId);
}
