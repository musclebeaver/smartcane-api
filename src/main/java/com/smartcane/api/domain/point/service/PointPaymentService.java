package com.smartcane.api.domain.point.service;

import com.smartcane.api.domain.identity.entity.User;
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
     * 회원 가입 직후 호출되어 포인트 지갑(계정)을 만들어 줍니다.
     * 이미 지갑이 존재하는 경우에는 중복 생성을 피하기 위해 기존 엔티티를 그대로 반환합니다.
     */
    @Transactional
    public PointAccount prepareAccount(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("포인트 지갑을 생성하려면 사용자 ID가 필요합니다.");
        }
        // 회원 지갑은 1:1 관계이므로 한 번만 생성되도록 방어 로직을 둡니다.
        return pointAccountRepository.findByUserId(user.getId())
                .orElseGet(() -> pointAccountRepository.save(new PointAccount(user)));
    }

    private PointAccount getAccountForUpdate(Long userId) {
        return pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PointAccountNotFoundException(userId));
    }

    /**
     * 읽기 전용 트랜잭션에서 사용할 포인트 계정을 조회합니다.
     */
    private PointAccount getAccount(Long userId) {
        return pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PointAccountNotFoundException(userId));
    }

    /**
     * 주어진 사용자에 대해 포인트 결제를 처리합니다.
     * 잔고 부족 시 {@link com.smartcane.api.domain.point.exception.PointInsufficientBalanceException}이 발생합니다.
     */
    @Transactional
    public long payWithPoint(Long userId, long amount) {
        PointAccount account = getAccountForUpdate(userId);
        account.deduct(amount);  // 실제 차감 로직은 엔티티가 담당합니다.
        return account.getBalance();
    }

    /**
     * 외부 결제 성공 등의 이벤트에 맞춰 포인트를 적립할 때 사용할 수 있습니다.
     */
    @Transactional
    public long accumulatePoint(Long userId, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 적립 금액은 음수일 수 없습니다.");
        }
        PointAccount account = getAccountForUpdate(userId);
        account.accumulate(amount);  // 적립 로직 역시 엔티티로 위임했습니다.
        return account.getBalance();
    }

    /**
     * 사용자가 직접 충전하는 시나리오에 맞춘 별도 진입점입니다.
     * accumulatePoint와 동일한 로직이지만, 의도를 드러내기 위해 메서드를 분리했습니다.
     */
    @Transactional
    public long chargePoint(Long userId, long amount) {
        return accumulatePoint(userId, amount);
    }

    @Transactional(readOnly = true)
    public long getPointBalance(Long userId) {
        // 조회 시에는 별도 갱신이 없으므로 읽기 전용 트랜잭션으로 처리합니다.
        return getAccount(userId).getBalance();
    }
}
