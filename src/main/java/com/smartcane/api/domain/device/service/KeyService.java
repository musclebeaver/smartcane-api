package com.smartcane.api.domain.device.service;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.smartcane.api.domain.device.dto.RotateKeyRequest;
import com.smartcane.api.domain.device.dto.RotateKeyResponse;
import com.smartcane.api.domain.device.entity.Device;
import com.smartcane.api.domain.device.entity.DeviceKey;
import com.smartcane.api.domain.device.repository.DeviceKeyRepository;
import com.smartcane.api.domain.device.repository.DeviceRepository;
import com.smartcane.api.security.util.KeyUtil;
import com.smartcane.api.domain.device.support.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.util.JSONObjectUtils;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyService {

    private final DeviceRepository deviceRepository;
    private final DeviceKeyRepository keyRepository;

    /**
     * 기존 active 키 비활성화 후 새 키 생성/활성화
     */
    @Transactional
    public RotateKeyResponse rotate(UUID deviceId, RotateKeyRequest req) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("디바이스를 찾을 수 없습니다: " + deviceId));

        // 기존 active 비활성화
        keyRepository.findByDeviceIdAndActive(device.getId(), true)
                .ifPresent(k -> k.setActive(false));

        // 새 키 생성
        String kid = KeyUtil.randomKid();
        JWK jwk = KeyUtil.generateJwk(req.algorithm(), kid);

        DeviceKey saved = keyRepository.save(DeviceKey.builder()
                .deviceId(device.getId())
                .kid(kid)
                .jwkPrivateJson(jwk.toJSONString())     // ⚠️ 운영: 암호화 저장
                .jwkPublicJson(jwk.toPublicJWK().toJSONString())
                .algorithm(req.algorithm().toUpperCase())
                .active(true)
                .build());

        // 단일 공개키 JWKS 반환(필요시 기기별 키목록으로 확장)
        String jwksJson = JSONObjectUtils.toJSONString(new JWKSet(jwk.toPublicJWK()).toJSONObject());
        return new RotateKeyResponse(saved.getKid(), jwksJson);
    }

    /**
     * 활성 공개키 JWKS 조회
     */
    @Transactional
    public String getActiveJwks(UUID deviceId) {
        DeviceKey key = keyRepository.findByDeviceIdAndActive(deviceId, true)
                .orElseThrow(() -> new NotFoundException("활성 키가 없습니다."));
        try {
            JWK publicJwk = JWK.parse(key.getJwkPublicJson());
            return JSONObjectUtils.toJSONString(new JWKSet(publicJwk).toJSONObject());
        } catch (Exception e) {
            throw new IllegalStateException("JWKS 생성/직렬화 실패", e);
        }
    }

    /**
     * 특정 kid 비활성화
     */
    @Transactional
    public void deactivate(UUID deviceId, String kid) {
        Optional<DeviceKey> found = keyRepository.findByKid(kid);
        DeviceKey key = found.filter(k -> k.getDeviceId().equals(deviceId))
                .orElseThrow(() -> new NotFoundException("해당 키를 찾을 수 없습니다."));
        key.setActive(false);
    }
}
