package com.smartcane.api.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * JwtTokenProvider (JJWT 0.12.x)
 *
 * 기능:
 *  - Access/Refresh 토큰 생성(typ 클레임 명시)
 *  - 파싱/검증(issuer, 서명, 만료, clock skew 허용)
 *  - 예외 타입 구분 (Expired, Malformed, Signature 등)
 *  - 헬퍼: userId(subject), roles, isRefresh
 */
@Component
public class JwtTokenProvider {

    private final String issuer;
    private final SecretKey key;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;
    private final long clockSkewSeconds; // 서버-클라 시간 오차 허용(초)

    public JwtTokenProvider(
            @Value("${smartcane.jwt.issuer}") String issuer,
            @Value("${smartcane.jwt.secret}") String secret,                 // 32바이트 이상 권장
            @Value("${smartcane.jwt.access-exp-seconds}") long accessExpSeconds,
            @Value("${smartcane.jwt.refresh-exp-seconds}") long refreshExpSeconds,
            @Value("${smartcane.jwt.clock-skew-seconds:60}") long clockSkewSeconds // 기본 60초
    ) {
        this.issuer = Objects.requireNonNull(issuer);
        // secret 인코딩: Base64 또는 원문 문자열 → HMAC 키 생성
        // 운영에서는 Base64로 전달하는 걸 권장(환경변수/시크릿)합니다.
        byte[] keyBytes = isLikelyBase64(secret)
                ? Decoders.BASE64.decode(secret)
                : secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;
        this.clockSkewSeconds = Math.max(0, clockSkewSeconds);
    }

    /** Access Token 생성 (typ=access) */
    public String generateAccessToken(Long userId, String email, Collection<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .header().type(Header.JWT_TYPE).and() // "typ":"JWT" 헤더
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .claim("email", email)
                .claim("roles", roles == null ? List.of() : roles)
                .claim("typ", "access") // payload typ
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Refresh Token 생성 (typ=refresh) */
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .header().type(Header.JWT_TYPE).and()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .claim("typ", "refresh")
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 서명/만료/issuer를 검증하며 파싱.
     * clockSkewSeconds 만큼 시간 오차를 허용.
     * 실패 시 JwtException(또는 하위 타입) 발생.
     */
    public Jws<Claims> parseAndValidate(String token) throws JwtException {
        try {
            return Jwts.parser()
                    .requireIssuer(issuer)
                    .clockSkewSeconds(clockSkewSeconds)
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            // 만료
            throw e;
        } catch (SecurityException e) {
            // 서명/무결성 관련 (SignatureException 포함)
            throw e;
        } catch (MalformedJwtException e) {
            // 형식 오류
            throw e;
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 JWT(압축 등)
            throw e;
        } catch (IllegalArgumentException e) {
            // 빈 토큰 등
            throw new MalformedJwtException("Illegal JWT: " + e.getMessage(), e);
        }
    }

    /** userId(subject) 반환 (검증 포함) */
    public Long getUserId(String token) {
        return Long.valueOf(parseAndValidate(token).getPayload().getSubject());
    }

    /** roles 클레임을 List<String>으로 반환 (검증 포함) */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object v = parseAndValidate(token).getPayload().get("roles");
        if (v == null) return List.of();
        if (v instanceof List<?> l) return l.stream().map(String::valueOf).toList();
        return List.of(String.valueOf(v));
    }

    /** typ이 refresh 인지 검사(검증 포함) */
    public boolean isRefreshToken(String token) {
        Object typ = parseAndValidate(token).getPayload().get("typ");
        return typ != null && "refresh".equalsIgnoreCase(String.valueOf(typ));
    }

    /** typ이 access 인지 검사(검증 포함) */
    public boolean isAccessToken(String token) {
        Object typ = parseAndValidate(token).getPayload().get("typ");
        return typ == null || "access".equalsIgnoreCase(String.valueOf(typ));
        // typ 미표기 토큰은 access로 간주(구버전 호환)
    }

    // ------------------------------------------------------
    // 유틸
    // ------------------------------------------------------

    private boolean isLikelyBase64(String s) {
        // 간단 추정: base64로 보이는지(패딩/문자셋) 체크
        // 보안 결정로직은 아님. 운영에서는 확실히 Base64로 관리하세요.
        if (s == null || s.length() < 32) return false;
        for (char c : s.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9') ||
                    c == '+' || c == '/' || c == '=')
                continue;
            return false;
        }
        return true;
    }
}
