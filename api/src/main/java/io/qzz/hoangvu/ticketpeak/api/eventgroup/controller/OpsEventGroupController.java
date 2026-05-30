package io.qzz.hoangvu.ticketpeak.api.eventgroup.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.dto.*;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.service.EventGroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ops/event-groups")
@PreAuthorize("hasRole('ADMIN')")
public class OpsEventGroupController {

    private final EventGroupService eventGroupService;

    public OpsEventGroupController(EventGroupService eventGroupService) {
        this.eventGroupService = eventGroupService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventGroupResponse>> createGroup(
            @Valid @RequestBody CreateEventGroupRequest request
    ) {
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
        Page<EventGroupResponse> response = eventGroupService.searchGroups(query, organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGroupResponse>> getGroup(@PathVariable UUID id) {
        EventGroupResponse response = eventGroupService.getGroup(id, true);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGroupResponse>> updateGroup(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventGroupRequest request
    ) {
        EventGroupResponse response = eventGroupService.updateGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Event Group updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable UUID id) {
        eventGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event Group deleted successfully"));
    }

    @PostMapping("/{id}/events")
    public ResponseEntity<ApiResponse<Void>> addEventToGroup(
            @PathVariable UUID id,
            @Valid @RequestBody AddEventToGroupRequest request
    ) {
        eventGroupService.addEventToGroup(id, request.eventId(), request.displayOrder());
        return ResponseEntity.ok(ApiResponse.success(null, "Event added to Group successfully"));
    }

    @DeleteMapping("/{id}/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> removeEventFromGroup(
            @PathVariable UUID id,
            @PathVariable UUID eventId
    ) {
        eventGroupService.removeEventFromGroup(id, eventId);
        return ResponseEntity.ok(ApiResponse.success(null, "Event removed from Group successfully"));
    }
}
