package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
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
        List<EventStatus> statuses = null;
        List<EventStatus> excludeStatuses = List.of(EventStatus.DRAFT); // Always exclude DRAFT from public search!

        if (status != null) {
            if (status == EventStatus.DRAFT) {
                // If DRAFT is requested, force empty results by passing empty statuses list
                statuses = List.of();
            } else {
                statuses = List.of(status);
            }
        }

        Page<EventResponse> response = eventService.searchEvents(
                query, statuses, excludeStatuses, venueId, organizationId,
                city, country, classificationId, attractionId,
                startAfter, startBefore, endAfter, endBefore,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable UUID id) {
        EventResponse response = eventService.getEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}/manifest")
    public ResponseEntity<ApiResponse<io.qzz.hoangvu.ticketpeak.api.event.dto.EventManifestDetailResponse>> getEventManifest(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventManifestDetail(id), "OK"));
    }
}
