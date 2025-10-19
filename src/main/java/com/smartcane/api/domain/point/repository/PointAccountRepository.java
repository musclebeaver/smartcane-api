package com.smartcane.api.domain.point.repository;

import com.smartcane.api.domain.point.entity.PointAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 포인트 계정을 조회/저장하기 위한 Spring Data JPA 리포지토리 인터페이스입니다.
 */
public interface PointAccountRepository extends JpaRepository<PointAccount, Long> {

    Optional<PointAccount> findByUserId(Long userId);
}
