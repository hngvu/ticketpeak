package io.qzz.hoangvu.ticketpeak.api.payment.service;

import java.util.Map;

public record WebhookContext(
    Map<String, String> params,   // VNPay uses this
    String rawBody,               // Stripe uses this
    Map<String, String> headers   // Stripe-Signature header
) {}
