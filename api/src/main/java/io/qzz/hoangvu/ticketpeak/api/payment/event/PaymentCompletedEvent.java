package io.qzz.hoangvu.ticketpeak.api.payment.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentCompletedEvent extends ApplicationEvent {

    private final UUID paymentId;
    private final UUID reservationId;
    private final UUID eventId;
    private final UUID accountId;
    private final BigDecimal amount;
    private final String currency;

    public PaymentCompletedEvent(
            Object source,
            UUID paymentId,
            UUID reservationId,
            UUID eventId,
            UUID accountId,
            BigDecimal amount,
            String currency
    ) {
        super(source);
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
