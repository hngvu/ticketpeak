package io.qzz.hoangvu.ticketpeak.api.eventgroup.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.dto.EventGroupResponse;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.service.EventGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/event-groups")
public class EventGroupController {

    private final EventGroupService eventGroupService;

    public EventGroupController(EventGroupService eventGroupService) {
        this.eventGroupService = eventGroupService;
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
        EventGroupResponse response = eventGroupService.getGroup(id, false);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<EventGroupResponse>> getGroupBySlug(@PathVariable String slug) {
        EventGroupResponse response = eventGroupService.getGroupBySlug(slug, false);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }
}
