// src/main/java/com/smartcane/api/config/OpenApiConfig.java
package com.smartcane.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartCaneOpenAPI() {
        return new OpenAPI()
                // ✅ JWT 보안 스키마 등록
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 인증 토큰을 입력하세요. (예: eyJhbGciOiJIUzI1NiIsInR5cCI6...)")
                        )
                )
                // ✅ API 기본 정보
                .info(new Info()
                        .title("SmartCane API")
                        .description("스마트 지팡이 교통결제 플랫폼 API 문서 (JWT 보안 적용)")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("SmartCane Team")
                                .email("support@smartcane.example"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // ✅ 서버 URL
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("로컬 개발 환경"),
                        new Server().url("https://api.smartcane.example").description("운영 서버")
                ))
                // ✅ 태그별 그룹
                .tags(List.of(
                        new Tag().name("Auth").description("회원가입/로그인/토큰 발급"),
                        new Tag().name("Payment").description("결제수단/정산"),
                        new Tag().name("Transit").description("승·하차, 노선/정류장 관리")
                ));
    }
}
