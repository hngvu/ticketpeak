package io.qzz.hoangvu.ticketpeak.api.venue.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public final class VenueException {
    private VenueException() {}

    public static ApiException notFound(UUID id) {
        return new ApiException(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND", "Venue not found: " + id);
    }

    public static ApiException manifestNotFound(String id) {
        return new ApiException(HttpStatus.NOT_FOUND, "MANIFEST_NOT_FOUND", "Manifest not found: " + id);
    }

    public static ApiException manifestIdExists(String id) {
        return new ApiException(HttpStatus.CONFLICT, "MANIFEST_ID_EXISTS", "Manifest with id '" + id + "' already exists");
    }

    public static ApiException invalidManifestStatus() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MANIFEST_STATUS", "Only DRAFT manifests can be published");
    }

    public static ApiException alreadyArchived() {
        return new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_ARCHIVED", "Manifest is already archived");
    }

    public static ApiException gaAreaIdExists(String id) {
        return new ApiException(HttpStatus.CONFLICT, "GA_AREA_ID_EXISTS", "GA area with id '" + id + "' already exists");
    }

    public static ApiException rsAreaNotFound(String id) {
        return new ApiException(HttpStatus.NOT_FOUND, "RS_AREA_NOT_FOUND", "RS area not found");
    }

    public static ApiException rsAreaIdExists(String id) {
        return new ApiException(HttpStatus.CONFLICT, "RS_AREA_ID_EXISTS", "RS area with id '" + id + "' already exists");
    }

    public static ApiException seatRowNotFound(String id) {
        return new ApiException(HttpStatus.NOT_FOUND, "SEAT_ROW_NOT_FOUND", "Seat row not found");
    }

    public static ApiException seatRowIdExists(String id) {
        return new ApiException(HttpStatus.CONFLICT, "SEAT_ROW_ID_EXISTS", "Seat row with id '" + id + "' already exists");
    }

    public static ApiException seatRowNameDuplicate(String name) {
        return new ApiException(HttpStatus.CONFLICT, "SEAT_ROW_NAME_DUPLICATE", "Seat row name '" + name + "' already exists in this area");
    }

    public static ApiException seatIdExists(String id) {
        return new ApiException(HttpStatus.CONFLICT, "SEAT_ID_EXISTS", "Seat with id '" + id + "' already exists");
    }

    public static ApiException seatNameDuplicate(String name) {
        return new ApiException(HttpStatus.CONFLICT, "SEAT_NAME_DUPLICATE", "Seat name '" + name + "' already exists in this row");
    }
}
