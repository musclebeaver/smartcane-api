package com.smartcane.api.domain.billing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossBillingClient {

    private final WebClient tossWebClient;

    /** 빌링키 발급 */
    public Map<String, Object> issueBillingKey(String authKey, String customerKey) {
        return tossWebClient.post()
                .uri("/v1/billing/authorizations/issue")
                .bodyValue(Map.of(
                        "authKey", authKey,
                        "customerKey", customerKey
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Toss issueBillingKey error", e)))
                .block();
    }

    /** 자동결제 */
    public Map<String, Object> requestPayment(String billingKey, Map<String, Object> body) {
        return tossWebClient.post()
                .uri("/v1/billing/{billingKey}", billingKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Toss requestPayment error", e)))
                .block();
    }

    /** 결제 취소 - Map 기반 */
    public Map<String, Object> cancelPayment(String paymentKey, Map<String, Object> body) {
        return tossWebClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Toss cancel error", e)))
                .block();
    }

    /** 오버로드 - 단순 사유만 전달 */
    public Map<String, Object> cancelPayment(String paymentKey, String cancelReason) {
        return cancelPayment(paymentKey, Map.of("cancelReason", cancelReason));
    }
}
