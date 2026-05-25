package io.qzz.hoangvu.ticketpeak.api.payment.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING, // Not actively used in V1, reserved for async gateways
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED,
    REFUNDED
}
