package io.qzz.hoangvu.ticketpeak.api.eventgroup.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public final class EventGroupException {
    private EventGroupException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "EVENT_GROUP_NOT_FOUND", "Event Group not found");
    }

    public static ApiException slugAlreadyExists() {
        return new ApiException(HttpStatus.CONFLICT, "EVENT_GROUP_SLUG_ALREADY_EXISTS", "Event Group slug already exists");
    }

    public static ApiException eventNotFound(UUID id) {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_NOT_FOUND", "Event with id " + id + " does not exist");
    }

    public static ApiException eventAlreadyInGroup() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_ALREADY_IN_GROUP", "Event is already in this group");
    }

    public static ApiException eventNotInGroup() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_NOT_IN_GROUP", "Event is not a member of this group");
    }
}
