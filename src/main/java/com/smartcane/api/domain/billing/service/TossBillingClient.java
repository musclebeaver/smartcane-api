package com.smartcane.api.domain.billing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossBillingClient {

    private final RestClient tossRestClient;

    private static final ParameterizedTypeReference<Map<String,Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    /** 빌링키 발급 */
    public Map<String, Object> issueBillingKey(String authKey, String customerKey) {
        return tossRestClient.post()
                .uri("/v1/billing/authorizations/issue")
                .body(Map.of("authKey", authKey, "customerKey", customerKey))
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) ->
                        new RuntimeException("Toss issueBillingKey HTTP " + res.getStatusCode()))
                .body(MAP_TYPE);
    }

    /** 자동결제 */
    public Map<String, Object> requestPayment(String billingKey, Map<String, Object> body) {
        return tossRestClient.post()
                .uri("/v1/billing/{billingKey}", billingKey)
                .body(body)
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) ->
                        new RuntimeException("Toss requestPayment HTTP " + res.getStatusCode()))
                .body(MAP_TYPE);
    }

    /** 결제 취소 - Map 기반 */
    public Map<String, Object> cancelPayment(String paymentKey, Map<String, Object> body) {
        return tossRestClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .body(body)
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) ->
                        new RuntimeException("Toss cancel HTTP " + res.getStatusCode()))
                .body(MAP_TYPE);
    }

    /** 오버로드 - 단순 사유만 전달 */
    public Map<String, Object> cancelPayment(String paymentKey, String cancelReason) {
        return cancelPayment(paymentKey, Map.of("cancelReason", cancelReason));
    }
}
