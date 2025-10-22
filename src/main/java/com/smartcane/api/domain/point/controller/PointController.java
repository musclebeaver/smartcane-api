package com.smartcane.api.domain.point.controller;

import com.smartcane.api.domain.point.dto.PointBalanceResponse;
import com.smartcane.api.domain.point.dto.PointChargeRequest;
import com.smartcane.api.domain.point.dto.PointPaymentRequest;
import com.smartcane.api.domain.point.service.PointPaymentService;
import com.smartcane.api.security.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 포인트 잔액 조회 및 충전 기능을 노출하는 프레젠테이션 레이어입니다.
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointPaymentService pointPaymentService;

    @GetMapping("/me")
    public PointBalanceResponse getMyBalance() {
        // SecurityContext에 저장된 현재 로그인 사용자의 ID를 가져옵니다.
        Long userId = AuthUtil.currentUserId();
        long balance = pointPaymentService.getPointBalance(userId);
        // 프론트엔드에서 금액만 필요하므로 단순한 응답 DTO로 감싸서 반환합니다.
        return new PointBalanceResponse(balance);
    }

    @PostMapping("/charge")
    public PointBalanceResponse chargePoint(@Valid @RequestBody PointChargeRequest request) {
        // 로그인 사용자 기준으로만 충전이 가능하도록 서버에서 사용자 ID를 강제합니다.
        Long userId = AuthUtil.currentUserId();
        long balance = pointPaymentService.chargePoint(userId, request.amount());
        // 충전 이후 갱신된 잔액을 바로 응답하여 포인트 탭에 표시할 수 있게 합니다.
        return new PointBalanceResponse(balance);
    }

    @PostMapping("/pay")
    public PointBalanceResponse payWithPoint(@Valid @RequestBody PointPaymentRequest request) {
        // 클라이언트가 결제하고자 하는 금액만 전달하면 서버에서 잔액 차감까지 처리합니다.
        Long userId = AuthUtil.currentUserId();
        long balance = pointPaymentService.payWithPoint(userId, request.amount());
        // 결제 후 남은 잔액을 즉시 반환해 프론트엔드에서 후속 처리를 쉽게 합니다.
        return new PointBalanceResponse(balance);
    }
}
