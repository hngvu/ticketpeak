package io.qzz.hoangvu.ticketpeak.api.venue.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.VenueResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.service.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VenueResponse>>> listVenues(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) VenueStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listVenues(name, status, pageable), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getVenue(id), "OK"));
    }
}
