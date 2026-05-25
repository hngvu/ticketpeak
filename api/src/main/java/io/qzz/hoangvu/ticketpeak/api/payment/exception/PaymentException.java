package io.qzz.hoangvu.ticketpeak.api.payment.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class PaymentException {

    private PaymentException() {}

    public static ApiException reservationNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found");
    }

    public static ApiException reservationNotPending() {
        return new ApiException(HttpStatus.CONFLICT, "RESERVATION_NOT_PENDING", "Reservation is not pending");
    }

    public static ApiException reservationExpired() {
        return new ApiException(HttpStatus.GONE, "RESERVATION_EXPIRED", "Reservation has expired");
    }

    public static ApiException reservationOwnerMismatch() {
        return new ApiException(HttpStatus.FORBIDDEN, "RESERVATION_OWNER_MISMATCH", "You do not own this reservation");
    }

    public static ApiException alreadyCompleted() {
        return new ApiException(HttpStatus.CONFLICT, "PAYMENT_ALREADY_COMPLETED", "This payment has already been completed");
    }

    public static ApiException invalidSignature() {
        return new ApiException(HttpStatus.BAD_REQUEST, "PAYMENT_INVALID_SIGNATURE", "Invalid payment gateway signature");
    }

    public static ApiException amountMismatch() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT_AMOUNT_MISMATCH", "Paid amount does not match reservation total");
    }

    public static ApiException gatewayError(String detail) {
        return new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR", detail);
    }

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment record not found");
    }

    public static ApiException unsupportedProvider() {
        return new ApiException(HttpStatus.BAD_REQUEST, "PAYMENT_UNSUPPORTED_PROVIDER", "Unsupported payment provider or currency mismatch");
    }
}
