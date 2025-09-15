package com.smartcane.api.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 개선사항 반영:
 *  - 이미 인증된 요청이면 스킵
 *  - Authorization: Bearer {access} 만 허용 (typ=refresh 토큰은 무시)
 *  - 예외 발생 시 401은 EntryPoint가 처리하도록 pass-through, 디버그 로깅 및 리퀘스트 속성 기록
 *  - 권한 컬렉션 타입을 Collection<? extends GrantedAuthority>로 수용
 *  - WebAuthenticationDetailsSource로 요청 details 세팅
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // 1) 이미 인증된 경우(예: 이전 필터에서 인증됨) 스킵
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.isAuthenticated()) {
            chain.doFilter(req, res);
            return;
        }

        // 2) Authorization 헤더에서 Bearer 토큰 추출
        String token = resolveBearerToken(req);

        if (token != null) {
            try {
                // 3) 파싱 & 검증 (서명/만료/issuer 등)
                var jws = tokenProvider.parseAndValidate(token);
                var claims = jws.getPayload();

                // 4) refresh 토큰 차단 (access만 허용)
                Object typ = claims.get("typ");
                if (typ != null && "refresh".equalsIgnoreCase(String.valueOf(typ))) {
                    if (log.isDebugEnabled()) log.debug("Ignoring refresh token on auth filter.");
                    chain.doFilter(req, res);
                    return;
                }

                // 5) userId(subject) + roles → 권한 목록
                Long userId = Long.valueOf(claims.getSubject());

                Collection<? extends GrantedAuthority> authorities =
                        tokenProvider.getRoles(token).stream()
                                .map(r -> r.startsWith("ROLE_") ? r : ("ROLE_" + r))
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                // 6) Authentication 구성 + 요청 details 세팅
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                // 토큰 불일치/만료/서명오류 등 → 미인증으로 계속 진행, EntryPoint가 401 처리
                if (log.isDebugEnabled()) {
                    log.debug("JWT validation failed: {}", e.toString());
                }
                // 진단용: 컨트롤러/EntryPoint에서 필요하면 참조 가능
                req.setAttribute("jwt.error", e.getClass().getSimpleName());
            }
        }

        // 7) 다음 필터로 진행
        chain.doFilter(req, res);
    }

    /**
     * Authorization: Bearer {token} 만 허용. 없으면 null.
     */
    private String resolveBearerToken(HttpServletRequest req) {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) return null;
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
