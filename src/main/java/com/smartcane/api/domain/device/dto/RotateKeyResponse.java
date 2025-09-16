package com.smartcane.api.domain.device.dto;

public record RotateKeyResponse(
        String kid,
        String jwks // 공개키 JWK Set(JSON)
) {}
