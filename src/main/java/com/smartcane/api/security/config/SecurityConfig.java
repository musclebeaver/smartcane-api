// src/main/java/com/smartcane/api/config/SecurityConfig.java
package com.smartcane.api.security.config;

import com.smartcane.api.security.jwt.JwtAuthenticationFilter;
import com.smartcane.api.security.jwt.JwtTokenProvider;
import com.smartcane.api.security.oauth.CustomOAuth2UserService;
import com.smartcane.api.security.oauth.OAuth2AuthenticationFailureHandler;
import com.smartcane.api.security.oauth.OAuth2AuthenticationSuccessHandler;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // 필수 컴포넌트
    private final JwtTokenProvider tokenProvider;
    private final RestAuthEntryPoint authEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    // 소셜(OAuth2) 사용할 때만 빈이 등록되어 있을 것
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST 기본 설정
                .csrf(csrf -> csrf.disable())                // 운영에선 필요시 특정 경로만 예외 처리로 전환
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리(401/403)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // 공개 엔드포인트
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/api/healthz",                       // 신규 헬스 체크 엔드포인트
                                "/api/auth/**",                               // 회원가입/로그인/리프레시/로그아웃
                                "/oauth2/**", "/login/oauth2/**", "/oauth2/authorization/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 API(예: 대중교통 공개)
                        .requestMatchers(HttpMethod.GET, "/api/transit/public/**").permitAll()

                        // 웹훅 (예: 토스 결제 웹훅)
                        .requestMatchers(HttpMethod.POST, "/api/billing/webhook").permitAll()

                        // 빌링 권한 예시
                        .requestMatchers(HttpMethod.POST, "/api/billing/issue").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/billing/pay").hasAnyRole("SYSTEM","ADMIN","USER")
                        .requestMatchers(HttpMethod.POST, "/api/billing/cancel").hasAnyRole("ADMIN","CS","SYSTEM")

                        // 그 외 보호
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인(있을 때만 동작)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );

        // JWT 필터
        http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS (운영에서는 Origin을 구체적으로 제한)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("*")); // 운영: 정확한 Origin 리스트로 교체
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of(
                "Authorization","Content-Type","X-Requested-With",
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
