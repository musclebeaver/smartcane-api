package com.smartcane.api.domain.device.controller;

import com.smartcane.api.domain.device.dto.RotateKeyRequest;
import com.smartcane.api.domain.device.dto.RotateKeyResponse;
import com.smartcane.api.domain.device.service.KeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/devices/{deviceId}")
public class KeyController {

    private final KeyService keyService;

    // 키 로테이션(활성화)
    @PostMapping("/keys/rotate")
    @ResponseStatus(HttpStatus.CREATED)
    public RotateKeyResponse rotate(@PathVariable UUID deviceId,
                                    @Valid @RequestBody RotateKeyRequest req) {
        return keyService.rotate(deviceId, req);
    }

    // 활성 공개키 JWKS
    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getActiveJwks(@PathVariable UUID deviceId) {
        return keyService.getActiveJwks(deviceId);
    }

    // 특정 kid 비활성화
    @PostMapping("/keys/{kid}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID deviceId, @PathVariable String kid) {
        keyService.deactivate(deviceId, kid);
    }
}
