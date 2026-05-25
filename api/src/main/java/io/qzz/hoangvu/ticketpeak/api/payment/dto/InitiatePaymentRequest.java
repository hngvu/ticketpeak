package io.qzz.hoangvu.ticketpeak.api.payment.dto;

import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InitiatePaymentRequest(
        @NotNull(message = "reservationId is required")
        UUID reservationId,

        @NotNull(message = "provider is required")
        PaymentProvider provider
) {}
