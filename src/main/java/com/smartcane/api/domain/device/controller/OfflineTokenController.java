package com.smartcane.api.domain.device.controller;

import com.smartcane.api.domain.device.dto.*;
import com.smartcane.api.domain.device.service.OfflineTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OfflineTokenController {

    private final OfflineTokenService tokenService;

    // 오프라인 토큰 발급
    @PostMapping("/api/devices/{deviceId}/offline-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueOfflineTokenResponse issue(@PathVariable UUID deviceId,
                                           @Valid @RequestBody IssueOfflineTokenRequest req) {
        return tokenService.issue(deviceId, req);
    }

    // 오프라인 토큰 검증
    @PostMapping("/api/devices/offline-tokens/verify")
    public VerifyOfflineTokenResponse verify(@Valid @RequestBody VerifyOfflineTokenRequest req) {
        return tokenService.verify(req);
    }

    // 오프라인 토큰 폐기
    @PostMapping("/api/devices/{deviceId}/offline-tokens/revoke")
    public RevokeOfflineTokenResponse revoke(@PathVariable UUID deviceId,
                                             @Valid @RequestBody RevokeOfflineTokenRequest req) {
        return tokenService.revoke(deviceId, req);
    }
}
