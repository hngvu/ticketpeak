package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.*;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partner/events")
public class PartnerEventController {

    private final EventService eventService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public PartnerEventController(
            EventService eventService,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository
    ) {
        this.eventService = eventService;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Event created successfully"));
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedAccount acc) {
            if (acc.role() != Role.ADMIN) {
                if (organizationId == null) {
                    throw new AccessDeniedException("Organization ID must be specified for non-admin search");
                }
                java.util.Set<UUID> memberOrgIds = new java.util.HashSet<>();
                organizationRepository.findByOwnerAccountId(acc.accountId())
                        .forEach(org -> memberOrgIds.add(org.getId()));
                organizationMemberRepository.findByAccountIdAndStatus(acc.accountId(), OrganizationMemberStatus.ACTIVE)
                        .forEach(m -> memberOrgIds.add(m.getOrganization().getId()));
                if (!memberOrgIds.contains(organizationId)) {
                    throw new AccessDeniedException("You do not have access to this organization");
                }
            }
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

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<EventResponse>> rescheduleEvent(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleEventRequest request
    ) {
        EventResponse response = eventService.rescheduleEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event rescheduled successfully"));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<EventResponse>> resumeEvent(@PathVariable UUID id) {
        EventResponse response = eventService.resumeEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Event resumed successfully"));
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
