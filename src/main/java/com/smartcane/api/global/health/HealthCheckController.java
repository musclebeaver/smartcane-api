package com.smartcane.api.global.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * 간단한 헬스 체크 컨트롤러.
 * 운영 환경에서 인프라 모니터링을 위해 상태값과 서버 시간을 함께 반환한다.
 */
@RestController
public class HealthCheckController {

    /**
     * /api/healthz 엔드포인트를 통해 서비스의 기초 상태를 확인한다.
     * 현재는 항상 200 OK와 간단한 JSON 바디를 응답하도록 구성하였다.
     */
    @GetMapping("/api/healthz")
    public ResponseEntity<Map<String, Object>> healthz() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",               // 상태 문자열
                        "timestamp", Instant.now().toString() // 서버 기준 타임스탬프
                )
        );
    }
}
