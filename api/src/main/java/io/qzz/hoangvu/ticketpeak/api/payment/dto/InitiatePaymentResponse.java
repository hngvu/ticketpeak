package io.qzz.hoangvu.ticketpeak.api.payment.dto;

import java.util.UUID;

public record InitiatePaymentResponse(
        UUID paymentId,
        String checkoutUrl,
        String clientSecret
) {}
