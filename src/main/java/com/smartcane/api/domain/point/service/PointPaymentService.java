package com.smartcane.api.domain.point.service;

import com.smartcane.api.domain.point.entity.PointAccount;
import com.smartcane.api.domain.point.exception.PointAccountNotFoundException;
import com.smartcane.api.domain.point.repository.PointAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 결제 및 적립을 담당하는 도메인 서비스의 스켈레톤입니다.
 */
@Service
public class PointPaymentService {

    private final PointAccountRepository pointAccountRepository;

    public PointPaymentService(PointAccountRepository pointAccountRepository) {
        this.pointAccountRepository = pointAccountRepository;
    }

    /**
     * 주어진 사용자에 대해 포인트 결제를 처리합니다.
     * 잔고 부족 시 {@link com.smartcane.api.domain.point.exception.PointInsufficientBalanceException}이 발생합니다.
     */
    @Transactional
    public long payWithPoint(Long userId, long amount) {
        PointAccount account = pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PointAccountNotFoundException(userId));

        account.deduct(amount);  // 실제 차감 로직은 엔티티가 담당합니다.
        return account.getBalance();
    }

    /**
     * 외부 결제 성공 등의 이벤트에 맞춰 포인트를 적립할 때 사용할 수 있습니다.
     */
    @Transactional
    public long accumulatePoint(Long userId, long amount) {
        PointAccount account = pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PointAccountNotFoundException(userId));

        account.accumulate(amount);  // 적립 로직 역시 엔티티로 위임했습니다.
        return account.getBalance();
    }
}
