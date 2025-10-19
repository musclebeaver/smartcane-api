package com.smartcane.api.domain.point.service;

import com.smartcane.api.domain.identity.entity.User;
import com.smartcane.api.domain.point.entity.PointAccount;
import com.smartcane.api.domain.point.exception.PointAccountNotFoundException;
import com.smartcane.api.domain.point.repository.PointAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointPaymentServiceTest {

    @Mock
    PointAccountRepository pointAccountRepository;

    @InjectMocks
    PointPaymentService pointPaymentService;

    private User newUser(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    @Test
    @DisplayName("지갑이 없는 사용자에 대해서는 새 포인트 지갑을 생성한다")
    void prepareAccount_createsNewAccountWhenMissing() {
        User user = newUser(1L);

        when(pointAccountRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(pointAccountRepository.save(any(PointAccount.class)))
                .thenAnswer(invocation -> invocation.<PointAccount>getArgument(0));

        PointAccount account = pointPaymentService.prepareAccount(user);

        assertThat(account.getUser()).isEqualTo(user);
        assertThat(account.getBalance()).isZero();
        verify(pointAccountRepository).save(any(PointAccount.class));
    }

    @Test
    @DisplayName("지갑이 이미 존재하면 추가 저장 없이 기존 엔티티를 재사용한다")
    void prepareAccount_reusesExistingAccount() {
        User user = newUser(2L);
        PointAccount existing = new PointAccount(user);

        when(pointAccountRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));

        PointAccount account = pointPaymentService.prepareAccount(user);

        assertThat(account).isSameAs(existing);
        verify(pointAccountRepository, never()).save(any(PointAccount.class));
    }

    @Nested
    class ChargePoint {

        @Test
        @DisplayName("충전 금액만큼 지갑 잔액을 증가시킨다")
        void increaseBalance() {
            User user = newUser(3L);
            PointAccount account = new PointAccount(user);

            when(pointAccountRepository.findByUserId(user.getId())).thenReturn(Optional.of(account));

            long balance = pointPaymentService.chargePoint(user.getId(), 1_000L);

            assertThat(balance).isEqualTo(1_000L);
            assertThat(account.getBalance()).isEqualTo(1_000L);
        }

        @Test
        @DisplayName("충전 대상 지갑이 없으면 예외를 던진다")
        void missingAccountThrows() {
            when(pointAccountRepository.findByUserId(4L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pointPaymentService.chargePoint(4L, 500L))
                    .isInstanceOf(PointAccountNotFoundException.class);
        }

        @Test
        @DisplayName("음수 금액 충전은 허용하지 않는다")
        void negativeAmountRejected() {
            assertThatThrownBy(() -> pointPaymentService.chargePoint(5L, -100L))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(pointAccountRepository);
        }
    }
}
