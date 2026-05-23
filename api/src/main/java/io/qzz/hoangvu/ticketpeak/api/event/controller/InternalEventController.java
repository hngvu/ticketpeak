package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.*;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/events")
public class InternalEventController {

    private final EventService eventService;

    public InternalEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> searchEvents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) UUID venueId,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) UUID classificationId,
            @RequestParam(required = false) UUID attractionId,
            @RequestParam(required = false) Instant startAfter,
            @RequestParam(required = false) Instant startBefore,
            @RequestParam(required = false) Instant endAfter,
            @RequestParam(required = false) Instant endBefore,
            Pageable pageable
    ) {
        List<EventStatus> statuses = status != null ? List.of(status) : null;

        Page<EventResponse> response = eventService.searchEvents(
                query, statuses, null, venueId, organizationId,
                city, country, classificationId, attractionId,
                startAfter, startBefore, endAfter, endBefore,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable UUID id) {
        EventResponse response = eventService.getEventForPartner(id);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event updated successfully"));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(@PathVariable UUID id) {
        EventResponse response = eventService.publishEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Event published successfully"));
    }

    @PostMapping("/{id}/postpone")
    public ResponseEntity<ApiResponse<EventResponse>> postponeEvent(
            @PathVariable UUID id,
            @Valid @RequestBody PostponeEventRequest request
    ) {
        EventResponse response = eventService.postponeEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event postponed successfully"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(@PathVariable UUID id) {
        EventResponse response = eventService.cancelEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Event cancelled successfully"));
    }

    @PostMapping("/{id}/onsale")
    public ResponseEntity<ApiResponse<EventResponse>> startEventSales(@PathVariable UUID id) {
        EventResponse response = eventService.startEventSales(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Event sales started successfully"));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<EventResponse>> cloneEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CloneEventRequest request
    ) {
        EventResponse response = eventService.cloneEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event cloned successfully"));
    }
}
