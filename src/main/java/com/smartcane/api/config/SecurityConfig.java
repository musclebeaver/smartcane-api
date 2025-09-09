// src/main/java/com/smartcane/api/config/SecurityConfig.java
package com.smartcane.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ✅ 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 인메모리 유저 (테스트용)
    //   ID: admin / PW: 123456
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("123456"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    // ✅ 기본 시큐리티 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 기본 설정
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CORS(프론트 로컬 허용)
                .cors(Customizer.withDefaults())

                // 요청 인가
                .authorizeHttpRequests(auth -> auth
                        // Swagger & OpenAPI → 모두 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 헬스체크, 공용 엔드포인트
                        .requestMatchers("/public/**", "/actuator/health").permitAll()

                        // OPTIONS 프리플라이트 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 테스트 편하게 HTTP Basic 활성화 (나중에 JWT로 교체)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
