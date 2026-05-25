package io.qzz.hoangvu.ticketpeak.api.payment.manager;

import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;

public record FinalizeContext(
    Payment payment,
    Reservation reservation,
    boolean alreadyCompleted
) {
    public static FinalizeContext createAlreadyCompleted() {
        return new FinalizeContext(null, null, true);
    }
    public static FinalizeContext of(Payment p, Reservation r) {
        return new FinalizeContext(p, r, false);
    }
}
