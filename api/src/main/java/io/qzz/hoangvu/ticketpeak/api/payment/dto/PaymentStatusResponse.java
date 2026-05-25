package io.qzz.hoangvu.ticketpeak.api.payment.dto;

import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentStatusResponse(
        UUID paymentId,
        UUID reservationId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        PaymentProvider provider,
        Instant createdAt
) {
    public static PaymentStatusResponse from(Payment payment) {
        return new PaymentStatusResponse(
                payment.getId(),
                payment.getReservationId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getCreatedAt()
        );
    }
}
