package io.qzz.hoangvu.ticketpeak.api.ticket.event;

import java.util.List;
import java.util.UUID;

public record TicketsVoidedEvent(List<UUID> ticketIds) {}
