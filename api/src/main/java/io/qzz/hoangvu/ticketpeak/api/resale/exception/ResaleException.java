package io.qzz.hoangvu.ticketpeak.api.resale.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public final class ResaleException extends ApiException {

    private ResaleException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static ResaleException resaleDisabled() {
        return new ResaleException(HttpStatus.BAD_REQUEST, "RESALE_DISABLED", "Resale is disabled for this event");
    }

    public static ResaleException ticketNotIssued() {
        return new ResaleException(HttpStatus.BAD_REQUEST, "TICKET_NOT_ISSUED", "Only issued tickets can be listed for resale");
    }

    public static ResaleException priceExceedsCap() {
        return new ResaleException(HttpStatus.BAD_REQUEST, "PRICE_EXCEEDS_CAP", "Asking price exceeds the event's resale price cap");
    }

    public static ResaleException activeListingExists() {
        return new ResaleException(HttpStatus.CONFLICT, "ACTIVE_LISTING_EXISTS", "This ticket already has an active or reserved listing");
    }

    public static ResaleException listingLimitExceeded() {
        return new ResaleException(HttpStatus.FORBIDDEN, "LISTING_LIMIT_EXCEEDED", "You have reached the maximum number of resale listings allowed for this event");
    }

    public static ResaleException noPrimaryPayoutMethod() {
        return new ResaleException(HttpStatus.BAD_REQUEST, "NO_PRIMARY_PAYOUT_METHOD", "You must configure a primary payout method before creating a resale listing");
    }

    public static ResaleException listingNotFound(UUID id) {
        return new ResaleException(HttpStatus.NOT_FOUND, "LISTING_NOT_FOUND", "Resale listing with id " + id + " not found");
    }

    public static ResaleException listingNotActive() {
        return new ResaleException(HttpStatus.CONFLICT, "LISTING_NOT_ACTIVE", "This listing is no longer active");
    }

    public static ResaleException cannotPurchaseOwnListing() {
        return new ResaleException(HttpStatus.FORBIDDEN, "CANNOT_PURCHASE_OWN_LISTING", "You cannot purchase your own listing");
    }

    public static ResaleException eventNotFound() {
        return new ResaleException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
    }
}
