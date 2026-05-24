package io.qzz.hoangvu.ticketpeak.api.event.model;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class EventStatusTransitionEvent extends ApplicationEvent {
    private final UUID eventId;
    private final EventStatus newStatus;

    public EventStatusTransitionEvent(Object source, UUID eventId, EventStatus newStatus) {
        super(source);
        this.eventId = eventId;
        this.newStatus = newStatus;
    }

    public UUID getEventId() {
        return eventId;
    }

    public EventStatus getNewStatus() {
        return newStatus;
    }
}
