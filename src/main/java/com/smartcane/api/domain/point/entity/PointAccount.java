package com.smartcane.api.domain.point.entity;

import com.smartcane.api.common.model.Auditable;
import com.smartcane.api.domain.identity.entity.User;
import com.smartcane.api.domain.point.exception.PointInsufficientBalanceException;
import jakarta.persistence.*;

/**
 * 사용자별 포인트 잔고를 관리하는 JPA 엔티티 스켈레톤입니다.
 * 추후 정산 이력과 적립 정책을 확장할 수 있도록 최소한의 필드만 구성했습니다.
 */
@Entity
@Table(name = "point_accounts")
public class PointAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                              // 고유 식별자

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;                            // 포인트를 보유한 사용자

    @Column(nullable = false)
    private long balance = 0L;                    // 보유 포인트 잔액

    protected PointAccount() {
        // JPA 기본 생성자
    }

    public PointAccount(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public long getBalance() {
        return balance;
    }

    /**
     * 포인트를 적립할 때 호출합니다.
     */
    public void accumulate(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 적립 금액은 음수일 수 없습니다.");
        }
        this.balance += amount;
    }

    /**
     * 포인트를 차감할 때 호출합니다. 잔고 부족 시 전용 예외를 발생시킵니다.
     */
    public void deduct(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 차감 금액은 음수일 수 없습니다.");
        }
        if (balance < amount) {
            throw new PointInsufficientBalanceException(balance, amount);
        }
        this.balance -= amount;
    }
}
