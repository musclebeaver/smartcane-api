package com.smartcane.api.security.config;

import com.smartcane.api.security.jwt.JwtAuthenticationFilter;
import com.smartcane.api.security.jwt.JwtTokenProvider;
import com.smartcane.api.security.web.RestAccessDeniedHandler;
import com.smartcane.api.security.web.RestAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final RestAuthEntryPoint authEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ✅ 운영 권장: CSRF 활성 + 웹훅만 예외 처리
                // .csrf(csrf -> csrf.ignoringRequestMatchers("/api/billing/webhook"))
                // 개발/POC: 전체 비활성
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(reg -> reg
                        // 공개 엔드포인트
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**","/swagger-ui/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/transit/public/**").permitAll()

                        // ✅ 웹훅: 토스 → 서버 (서명검증은 앱 내부에서 수행)
                        .requestMatchers(HttpMethod.POST, "/api/billing/webhook").permitAll()

                        // ✅ 빌링: 최초 빌링키 발급(사용자 본인)
                        .requestMatchers(HttpMethod.POST, "/api/billing/issue").hasAnyRole("USER","ADMIN")

                        // ✅ 자동결제 트리거: 보통 서버 배치/시스템 계정이 호출
                        .requestMatchers(HttpMethod.POST, "/api/billing/pay").hasAnyRole("SYSTEM","ADMIN","USER")
                        // 실무에선 USER 직접 호출을 금지하고, 정산 배치만 호출하도록 좁히는 것도 좋음:
                        // .requestMatchers(HttpMethod.POST, "/api/billing/pay").hasAnyRole("SYSTEM","ADMIN")

                        // ✅ 취소(환불): 더 보수적으로
                        .requestMatchers(HttpMethod.POST, "/api/billing/cancel").hasAnyRole("ADMIN","CS","SYSTEM")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("*")); // 운영: 정확한 Origin만 나열
        cfg.setAllowedMethods(List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of(
                "Authorization","Content-Type","X-Requested-With",
                // (참고) 웹훅은 서버→서버 호출이라 CORS 대상 아님. 그래도 열어두어 무해.
                "tosspayments-webhook-signature",
                "tosspayments-webhook-transmission-time",
                "tosspayments-webhook-transmission-id",
                "tosspayments-webhook-transmission-retried-count"
        ));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
