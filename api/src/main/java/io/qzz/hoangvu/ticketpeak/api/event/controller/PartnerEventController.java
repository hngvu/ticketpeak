package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.*;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partner/events")
public class PartnerEventController {

    private final EventService eventService;

    public PartnerEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.createEvent(request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Event created successfully"));
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
            Pageable pageable,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);

        if (principal.role() != Role.ADMIN && organizationId == null) {
            throw new AccessDeniedException("Organization ID must be specified for non-admin search");
        }

        List<EventStatus> statuses = null;
        if (status != null) {
            statuses = List.of(status);
        }

        Page<EventResponse> response = eventService.searchEvents(
                query, statuses, null, venueId, organizationId,
                city, country, classificationId, attractionId,
                startAfter, startBefore, endAfter, endBefore,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.getEventForPartner(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.updateEvent(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Event updated successfully"));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.publishEvent(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Event published successfully"));
    }

    @PostMapping("/{id}/postpone")
    public ResponseEntity<ApiResponse<EventResponse>> postponeEvent(
            @PathVariable UUID id,
            @Valid @RequestBody PostponeEventRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.postponeEvent(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Event postponed successfully"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.cancelEvent(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Event cancelled successfully"));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<EventResponse>> cloneEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CloneEventRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        EventResponse response = eventService.cloneEvent(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Event cloned successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = getVerifiedOrganizer(authentication);
        eventService.deleteEvent(id, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }

    private AuthenticatedAccount getVerifiedOrganizer(Authentication authentication) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        if (principal.role() != Role.ORGANIZER && principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only organizers and admins can access partner endpoints");
        }
        return principal;
    }
}
