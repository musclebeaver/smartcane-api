package com.smartcane.api.domain.device.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;
import com.smartcane.api.domain.device.dto.*;
import com.smartcane.api.domain.device.entity.Device;
import com.smartcane.api.domain.device.entity.DeviceKey;
import com.smartcane.api.domain.device.entity.OfflineToken;
import com.smartcane.api.domain.device.repository.DeviceKeyRepository;
import com.smartcane.api.domain.device.repository.DeviceRepository;
import com.smartcane.api.domain.device.repository.OfflineTokenRepository;
import com.smartcane.api.domain.device.support.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineTokenService {

    private final DeviceRepository deviceRepository;
    private final DeviceKeyRepository keyRepository;
    private final OfflineTokenRepository tokenRepository;

    /**
     * 활성 키로 오프라인 토큰(JWS/JWT) 발급
     */
    @Transactional
    public IssueOfflineTokenResponse issue(UUID deviceId, IssueOfflineTokenRequest req) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("디바이스를 찾을 수 없습니다: " + deviceId));

        DeviceKey key = keyRepository.findByDeviceIdAndActive(device.getId(), true)
                .orElseThrow(() -> new NotFoundException("활성 키가 없습니다. 먼저 키를 로테이션하세요."));

        try {
            JWK jwk = JWK.parse(key.getJwkPrivateJson());
            JWSHeader header = switch (key.getAlgorithm()) {
                case "ED25519" -> new JWSHeader.Builder(JWSAlgorithm.EdDSA).keyID(key.getKid()).type(JOSEObjectType.JWT).build();
                case "ES256"   -> new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(key.getKid()).type(JOSEObjectType.JWT).build();
                default -> throw new IllegalArgumentException("지원하지 않는 알고리즘: " + key.getAlgorithm());
            };

            Instant now = Instant.now();
            Instant exp = now.plus(req.ttlSeconds(), ChronoUnit.SECONDS);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(device.getId().toString())
                    .issuer("smartcane-api")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .claim("scope", req.scope())
                    .claim("device_serial", device.getSerialNo())
                    .build();

            SignedJWT signed = new SignedJWT(header, claims);

            JWSSigner signer = switch (key.getAlgorithm()) {
                case "ED25519" -> new Ed25519Signer((OctetKeyPair) jwk);
                case "ES256"   -> new ECDSASigner((ECKey) jwk);
                default -> throw new IllegalArgumentException("지원하지 않는 알고리즘: " + key.getAlgorithm());
            };

            signed.sign(signer);
            String compact = signed.serialize();

            OfflineToken saved = tokenRepository.save(OfflineToken.builder()
                    .deviceId(device.getId())
                    .token(compact)
                    .scope(req.scope())
                    .expiresAt(exp)
                    .revoked(false)
                    .build());

            return new IssueOfflineTokenResponse(saved.getToken(), saved.getExpiresAt());

        } catch (Exception e) {
            throw new IllegalStateException("오프라인 토큰 발급 실패", e);
        }
    }

    /**
     * 토큰 유효성 검증(JWS 검증 + 만료/폐기 확인)
     */
    @Transactional
    public VerifyOfflineTokenResponse verify(VerifyOfflineTokenRequest req) {
        try {
            SignedJWT jwt = SignedJWT.parse(req.token());
            JWSHeader header = jwt.getHeader();
            String kid = header.getKeyID();

            DeviceKey key = keyRepository.findByKid(kid)
                    .orElseThrow(() -> new NotFoundException("kid에 해당하는 키가 없습니다: " + kid));

            JWK publicJwk = JWK.parse(key.getJwkPublicJson());
            JWSVerifier verifier = switch (key.getAlgorithm()) {
                case "ED25519" -> new Ed25519Verifier((OctetKeyPair) publicJwk); // public 부분만 사용됨
                case "ES256"   -> new ECDSAVerifier(((ECKey) publicJwk).toECPublicKey());
                default -> throw new IllegalArgumentException("지원하지 않는 알고리즘: " + key.getAlgorithm());
            };

            boolean sigOk = jwt.verify(verifier);
            if (!sigOk) return new VerifyOfflineTokenResponse(false, "서명 검증 실패");

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Instant now = Instant.now();
            if (claims.getExpirationTime() == null || now.isAfter(claims.getExpirationTime().toInstant())) {
                return new VerifyOfflineTokenResponse(false, "토큰 만료");
            }

            UUID deviceId = UUID.fromString(claims.getSubject());
            // 저장소에 존재 & revoked 여부 확인(옵션)
            return tokenRepository.findByDeviceIdAndToken(deviceId, req.token())
                    .map(t -> t.isRevoked()
                            ? new VerifyOfflineTokenResponse(false, "토큰 폐기됨")
                            : new VerifyOfflineTokenResponse(true, "OK"))
                    .orElse(new VerifyOfflineTokenResponse(false, "발급 이력 없음"));

        } catch (NotFoundException nf) {
            return new VerifyOfflineTokenResponse(false, nf.getMessage());
        } catch (Exception e) {
            return new VerifyOfflineTokenResponse(false, "검증 실패");
        }
    }

    /**
     * 토큰 폐기(블랙리스트)
     */
    @Transactional
    public RevokeOfflineTokenResponse revoke(UUID deviceId, RevokeOfflineTokenRequest req) {
        OfflineToken tok = tokenRepository.findByDeviceIdAndToken(deviceId, req.token())
                .orElseThrow(() -> new NotFoundException("토큰을 찾을 수 없습니다."));

        tok.setRevoked(true);
        return new RevokeOfflineTokenResponse(true);
    }
}
