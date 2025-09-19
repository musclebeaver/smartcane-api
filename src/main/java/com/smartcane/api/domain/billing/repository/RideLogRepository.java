package com.smartcane.api.domain.billing.repository;

import com.smartcane.api.domain.billing.entity.RideLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface RideLogRepository extends JpaRepository<RideLog, Long> {

    /** 특정 사용자의 기간 내 탑승 이력 (시작시각 기준 정렬) */
    List<RideLog> findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(
            Long userId, OffsetDateTime from, OffsetDateTime to
    );

    /** 기간 내 전체 탑승 이력 — 사용자별 그룹핑은 서비스에서 */
    List<RideLog> findByStartedAtBetweenOrderByUserIdAscStartedAtAsc(
            OffsetDateTime from, OffsetDateTime to
    );
}
