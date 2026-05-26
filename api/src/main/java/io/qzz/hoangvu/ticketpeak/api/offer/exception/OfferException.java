package io.qzz.hoangvu.ticketpeak.api.offer.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class OfferException {
    private OfferException() {}

    public static ApiException invalidEventState(String reason) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_STATE", reason);
    }

    public static ApiException ticketTypeAlreadyExists() {
        return new ApiException(HttpStatus.CONFLICT, "TICKET_TYPE_ALREADY_EXISTS", "Ticket type slug is already registered for this event");
    }

    public static ApiException ticketTypeNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "TICKET_TYPE_NOT_FOUND", "Ticket type does not exist");
    }

    public static ApiException ticketTypeInUse(String message) {
        return new ApiException(HttpStatus.CONFLICT, "TICKET_TYPE_IN_USE", message);
    }

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "OFFER_NOT_FOUND", "Offer does not exist for this event");
    }

    public static ApiException invalidCurrency() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY", "Currency must be a valid ISO 4217 code");
    }

    public static ApiException invalidPrice() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_PRICE", "Face value must be greater than or equal to 0");
    }

    public static ApiException invalidLimits() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_LIMITS", "Event ticket minimum must be greater than or equal to 1");
    }

    public static ApiException invalidWindow(String reason) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW", reason);
    }

    public static ApiException invalidQuantity(String reason) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_QUANTITY", reason);
    }

    public static ApiException invalidSeating(String reason) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_SEATING", reason);
    }

    public static ApiException noPublishedManifest() {
        return new ApiException(HttpStatus.BAD_REQUEST, "NO_PUBLISHED_MANIFEST", "The assigned venue does not have a published manifest");
    }

    public static ApiException sectionNotFound(String sectionId, String manifestId) {
        return new ApiException(HttpStatus.BAD_REQUEST, "SECTION_NOT_FOUND", "Section with id '" + sectionId + "' does not exist in manifest '" + manifestId + "'");
    }

    public static ApiException priceLevelNotFound(String priceLevelId, String manifestId) {
        return new ApiException(HttpStatus.BAD_REQUEST, "PRICE_LEVEL_NOT_FOUND", "Price level with id '" + priceLevelId + "' does not exist in manifest '" + manifestId + "'");
    }
}
