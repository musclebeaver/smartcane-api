package com.smartcane.api.domain.billing.util;

import com.smartcane.api.domain.billing.dto.WebhookEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class WebhookVerifier {

    @Value("${billing.webhook.secret}")
    private String webhookSecret; // 토스 개발자센터에서 발급/설정한 보안 키

    /**
     * 토스 문서 기준 검증:
     * 1) message = rawBody + ":" + transmissionTime
     * 2) HMAC-SHA256(message, secret) 계산 → 바이트 배열
     * 3) 서명 헤더(tosspayments-webhook-signature)의 "v1:..." 2개 값을 Base64 decode
     * 4) (2)와 (3) 중 하나라도 바이트 동일하면 유효
     */
    public void verifyOrThrow(WebhookEvent event) {
        // 결제 웹훅 등 일부 이벤트에선 서명 헤더가 없으므로, 존재할 때만 검증
        if (event.signature() == null || event.signature().isBlank()) {
            return; // 서명이 없는 유형은 스킵(로그만 남기는 것을 권장)
        }
        if (event.transmissionTime() == null || event.transmissionTime().isBlank()) {
            throw new IllegalArgumentException("Missing transmission time header");
        }

        try {
            // 1) message 구성
            String message = event.rawBody() + ":" + event.transmissionTime();

            // 2) HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256"));
            byte[] expected = mac.doFinal(message.getBytes());

            // 3) 헤더에서 v1 서명들 추출 → Base64 decode (예: "v1:xxx, v1:yyy")
            List<byte[]> provided = parseAndDecodeV1Signatures(event.signature());

            // 4) 비교 (어느 하나라도 동일하면 통과)
            boolean match = provided.stream().anyMatch(sig -> constantTimeEquals(sig, expected));
            if (!match) {
                throw new IllegalArgumentException("Invalid webhook signature");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Webhook verification failed", e);
        }
    }

    private List<byte[]> parseAndDecodeV1Signatures(String signatureHeader) {
        List<byte[]> result = new ArrayList<>();
        if (signatureHeader == null) return result;

        // 헤더는 콤마(,)로 여러 값이 올 수 있음. 각 항목은 "v1:BASE64" 형태
        String[] parts = signatureHeader.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            int idx = trimmed.indexOf("v1:");
            if (idx >= 0) {
                String b64 = trimmed.substring(idx + 3).trim();
                try {
                    result.add(Base64.getDecoder().decode(b64));
                } catch (IllegalArgumentException ignore) {
                    // 디코딩 실패 항목은 무시
                }
            }
        }
        return result;
    }

    /** 타이밍 공격 방지용 상수 시간 비교 */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int res = 0;
        for (int i = 0; i < a.length; i++) res |= (a[i] ^ b[i]);
        return res == 0;
    }
}
