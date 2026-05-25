package io.qzz.hoangvu.ticketpeak.api.payment.service;

import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;

public interface PaymentWebhookHandler {
    PaymentProvider getProvider();
    void handleWebhook(WebhookContext context);
}
