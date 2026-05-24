package io.qzz.hoangvu.ticketpeak.api.event.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public final class EventException {
    private EventException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
    }

    public static ApiException notPublishable() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_NOT_PUBLISHABLE", "Only DRAFT events can be published");
    }

    public static ApiException cannotUpdateEnded() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_UPDATE_ENDED", "Cannot update ended or cancelled event");
    }

    public static ApiException cannotChangeVenue() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_CHANGE_VENUE", "Cannot change venue for a published or active event");
    }

    public static ApiException cannotPostpone() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_POSTPONE", "Only published, on-sale, or postponed events can be postponed");
    }

    public static ApiException cannotCancel() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_CANCEL", "Event is already in ended or cancelled state");
    }

    public static ApiException cannotReschedule() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_RESCHEDULE", "Only postponed or rescheduled events can be rescheduled");
    }

    public static ApiException cannotResume() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_RESUME", "Only postponed or rescheduled events can be resumed");
    }

    public static ApiException cannotStartSales() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_CANNOT_START_SALES", "Only PUBLISHED or OFFSALE events can start sales");
    }

    public static ApiException invalidDates(String reason) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", reason);
    }

    public static ApiException missingVenue() {
        return new ApiException(HttpStatus.BAD_REQUEST, "MISSING_VENUE", "Event must have an assigned venue to be published");
    }

    public static ApiException noPublishedManifest() {
        return new ApiException(HttpStatus.BAD_REQUEST, "NO_PUBLISHED_MANIFEST", "The assigned venue does not have a published manifest");
    }

    public static ApiException venueNotFound() {
        return new ApiException(HttpStatus.BAD_REQUEST, "VENUE_NOT_FOUND", "The assigned venue does not exist");
    }

    public static ApiException attractionNotFound(UUID id) {
        return new ApiException(HttpStatus.BAD_REQUEST, "ATTRACTION_NOT_FOUND", "Attraction with id " + id + " does not exist");
    }

    public static ApiException attractionNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "ATTRACTION_NOT_FOUND", "Attraction not found");
    }

    public static ApiException classificationNotFound(UUID id) {
        return new ApiException(HttpStatus.BAD_REQUEST, "CLASSIFICATION_NOT_FOUND", "Classification with id " + id + " does not exist");
    }

    public static ApiException classificationNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "CLASSIFICATION_NOT_FOUND", "Classification not found");
    }

    public static ApiException slugAlreadyExists(String message) {
        return new ApiException(HttpStatus.CONFLICT, "SLUG_ALREADY_EXISTS", message);
    }

    public static ApiException parentNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "PARENT_NOT_FOUND", "Parent classification not found");
    }

    public static ApiException invalidLevel() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LEVEL", "Child level must be greater than parent level");
    }

    public static ApiException invalidParent() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PARENT", "A classification cannot be its own parent");
    }
}
