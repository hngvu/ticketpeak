package io.qzz.hoangvu.ticketpeak.api.reservation.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class ReservationException {

    private ReservationException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found");
    }

    public static ApiException alreadyFinalized() {
        return new ApiException(HttpStatus.CONFLICT, "RESERVATION_ALREADY_FINALIZED", "Reservation is already confirmed, expired, or cancelled");
    }

    public static ApiException expired() {
        return new ApiException(HttpStatus.GONE, "RESERVATION_EXPIRED", "Reservation has expired");
    }

    public static ApiException invalidItem(String detail) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_INVALID_ITEM", detail);
    }

    public static ApiException eventNotOnSale() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EVENT_NOT_ON_SALE", "Event is not currently accepting reservations");
    }

    public static ApiException offerNotInSaleWindow() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "OFFER_NOT_IN_SALE_WINDOW", "One or more offers are not in an active sale window");
    }

    public static ApiException invalidQuantity(String detail) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_INVALID_QUANTITY", detail);
    }

    public static ApiException offerNotFound() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_OFFER_NOT_FOUND", "Offer not found for this event");
    }

    public static ApiException currencyMismatch() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "RESERVATION_CURRENCY_MISMATCH", "All items in a reservation must use the same currency");
    }

    public static ApiException seatUnavailable(String seatId) {
        return new ApiException(HttpStatus.CONFLICT, "RESERVATION_SEAT_UNAVAILABLE", "Seat " + seatId + " is not available");
    }

    public static ApiException gaCapacityInsufficient(String areaId) {
        return new ApiException(HttpStatus.CONFLICT, "RESERVATION_GA_CAPACITY_INSUFFICIENT", "Insufficient capacity in area " + areaId);
    }
}
