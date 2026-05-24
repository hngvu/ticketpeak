package io.qzz.hoangvu.ticketpeak.api.inventory.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class InventoryException {
    private InventoryException() {}

    public static ApiException invalidOfferMapping(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_MAPPING", message);
    }

    public static ApiException inventoryNotInitialized() {
        return new ApiException(HttpStatus.CONFLICT, "INVENTORY_NOT_INITIALIZED", "Inventory not yet available for this event");
    }

    public static ApiException insufficientGaCapacity(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_GA_CAPACITY", message);
    }

    public static ApiException invalidRelease(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RELEASE", message);
    }

    public static ApiException insufficientGaHeld(String message) {
        return new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_GA_HELD", message);
    }

    public static ApiException invalidRefund(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REFUND", message);
    }

    public static ApiException seatNotAvailable(String message) {
        return new ApiException(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", message);
    }

    public static ApiException invalidQuantity() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", "Quantity must be greater than 0");
    }
}
