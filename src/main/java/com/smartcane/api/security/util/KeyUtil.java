package com.smartcane.api.security.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.Base64URL;

import java.security.SecureRandom;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.util.JSONObjectUtils;

public final class KeyUtil {

    private static final SecureRandom RAND = new SecureRandom();

    private KeyUtil() {}

    /** 랜덤 KID 생성 (URL-safe Base64) */
    public static String randomKid() {
        byte[] b = new byte[8];
        RAND.nextBytes(b);
        return Base64URL.encode(b).toString();
    }

    /** 알고리즘에 따른 JWK(KeyPair) 생성 */
    public static JWK generateJwk(String algorithm, String kid) {
        try {
            return switch (algorithm.toUpperCase()) {
                case "ED25519" -> new OctetKeyPairGenerator(Curve.Ed25519).keyID(kid).generate();
                case "ES256"   -> new ECKeyGenerator(Curve.P_256).keyID(kid).generate();
                default        -> throw new IllegalArgumentException("지원하지 않는 알고리즘: " + algorithm);
            };
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 공개키만 포함한 단일 JWKS(JSON) */
    public static String toPublicJwksJson(JWK jwk) {
        return JSONObjectUtils.toJSONString(new JWKSet(jwk.toPublicJWK()).toJSONObject());
    }
}
