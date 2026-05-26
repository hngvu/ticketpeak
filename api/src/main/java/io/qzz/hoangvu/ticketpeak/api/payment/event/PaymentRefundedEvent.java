package io.qzz.hoangvu.ticketpeak.api.payment.event;

import java.util.UUID;

public record PaymentRefundedEvent(
        UUID orderId,
        UUID accountId
) {}
