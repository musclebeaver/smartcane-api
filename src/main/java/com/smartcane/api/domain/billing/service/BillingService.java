package com.smartcane.api.domain.billing.service;

import com.smartcane.api.domain.billing.dto.*;

public interface BillingService {
    BillingKeyIssueResponse issueBillingKey(BillingKeyIssueRequest req);
    AutoPayResponse autoPay(AutoPayRequest req);
    void cancel(CancelRequest req);
    void handleWebhook(WebhookEvent event);
}
