package com.smartcane.api.domain.billing.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcane.api.domain.billing.dto.*;
import com.smartcane.api.domain.billing.service.BillingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/issue")
    public BillingKeyIssueResponse issue(@Valid @RequestBody BillingKeyIssueRequest req) {
        return billingService.issueBillingKey(req);
    }

    @PostMapping("/pay")
    public AutoPayResponse pay(@Valid @RequestBody AutoPayRequest req) {
        return billingService.autoPay(req);
    }

    @PostMapping("/cancel")
    public void cancel(@Valid @RequestBody CancelRequest req) {
        billingService.cancel(req);
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void webhook(@RequestBody String rawBody, HttpServletRequest request) throws Exception {
        // ✅ 토스 문서 기준 실제 헤더명
        String signature       = request.getHeader("tosspayments-webhook-signature");
        String transmissionTime= request.getHeader("tosspayments-webhook-transmission-time");
        String transmissionId  = request.getHeader("tosspayments-webhook-transmission-id");
        String retriedCount    = request.getHeader("tosspayments-webhook-transmission-retried-count");

        JsonNode root = objectMapper.readTree(rawBody);
        WebhookEvent event = new WebhookEvent(
                root.path("eventType").asText(null),
                root.path("paymentKey").asText(null),
                root.path("orderId").asText(null),
                root.path("status").asText(null),
                signature,
                transmissionTime,
                transmissionId,
                retriedCount,
                rawBody
        );

        billingService.handleWebhook(event);
    }
}
