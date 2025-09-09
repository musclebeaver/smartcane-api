// src/main/java/com/smartcane/api/controller/HelloController.java
package com.smartcane.api.swaggercontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hello", description = "샘플 API")
@RestController
public class HelloController {

    @Operation(summary = "공개 핑", description = "인증 없이 호출 가능한 공개 API")
    @GetMapping("/public/ping")
    public String publicPing() {
        return "pong-public";
    }

    @Operation(summary = "보호 핑", description = "인증 필요(API 보안 테스트용)")
    @GetMapping("/api/secure-ping")
    public String securePing() {
        return "pong-secure";
    }
}
