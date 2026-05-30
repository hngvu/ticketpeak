package io.qzz.hoangvu.ticketpeak.api.eventgroup.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.dto.*;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.exception.EventGroupException;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroup;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.repository.EventGroupRepository;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.service.EventGroupService;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import io.qzz.hoangvu.ticketpeak.api.security.OrgSecurity;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/partner/event-groups")
public class PartnerEventGroupController {

    private final EventGroupService eventGroupService;
    private final EventGroupRepository eventGroupRepository;
    private final EventRepository eventRepository;
    private final OrgSecurity orgSecurity;

    public PartnerEventGroupController(
            EventGroupService eventGroupService,
            EventGroupRepository eventGroupRepository,
            EventRepository eventRepository,
            OrgSecurity orgSecurity
    ) {
        this.eventGroupService = eventGroupService;
        this.eventGroupRepository = eventGroupRepository;
        this.eventRepository = eventRepository;
        this.orgSecurity = orgSecurity;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventGroupResponse>> createGroup(
            @Valid @RequestBody CreateEventGroupRequest request
    ) {
        validateOrgAccess(request.organizationId());
        EventGroupResponse response = eventGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Event Group created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventGroupResponse>>> searchGroups(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID organizationId,
            Pageable pageable
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedAccount acc) {
            if (acc.role() != Role.ADMIN) {
                if (organizationId == null) {
                    throw new AccessDeniedException("Organization ID must be specified for non-admin search");
                }
                if (!orgSecurity.isOwnerOrMember(organizationId)) {
                    throw new AccessDeniedException("You do not have access to this organization");
                }
            }
        }

        Page<EventGroupResponse> response = eventGroupService.searchGroups(query, organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGroupResponse>> getGroup(@PathVariable UUID id) {
        validateGroupAccess(id);
        EventGroupResponse response = eventGroupService.getGroup(id, true);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGroupResponse>> updateGroup(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventGroupRequest request
    ) {
        validateGroupAccess(id);
        EventGroupResponse response = eventGroupService.updateGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event Group updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable UUID id) {
        validateGroupAccess(id);
        eventGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event Group deleted successfully"));
    }

    @PostMapping("/{id}/events")
    public ResponseEntity<ApiResponse<Void>> addEventToGroup(
            @PathVariable UUID id,
            @Valid @RequestBody AddEventToGroupRequest request
    ) {
        validateGroupAccess(id);
        
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> EventGroupException.eventNotFound(request.eventId()));
        validateOrgAccess(event.getOrganizationId());

        eventGroupService.addEventToGroup(id, request.eventId(), request.displayOrder());
        return ResponseEntity.ok(ApiResponse.success(null, "Event added to Group successfully"));
    }

    @DeleteMapping("/{id}/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> removeEventFromGroup(
            @PathVariable UUID id,
            @PathVariable UUID eventId
    ) {
        validateGroupAccess(id);
        eventGroupService.removeEventFromGroup(id, eventId);
        return ResponseEntity.ok(ApiResponse.success(null, "Event removed from Group successfully"));
    }

    private void validateOrgAccess(UUID organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedAccount acc) {
            if (acc.role() != Role.ADMIN && !orgSecurity.isOwnerOrMember(organizationId)) {
                throw new AccessDeniedException("You do not have access to this organization");
            }
        }
    }

    private void validateGroupAccess(UUID groupId) {
        EventGroup group = eventGroupRepository.findById(groupId)
                .orElseThrow(EventGroupException::notFound);
        validateOrgAccess(group.getOrganizationId());
    }
}
