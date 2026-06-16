package io.qzz.hoangvu.ticketpeak.api.venue.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.ManifestResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.LevelResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.SectionResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.PriceLevelResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.SeatRowResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.SeatResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partner/venues")
public class PartnerVenueController {

    private final VenueService venueService;

    public PartnerVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/{venueId}/manifests")
    public ResponseEntity<ApiResponse<List<ManifestResponse>>> listManifests(@PathVariable UUID venueId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listManifests(venueId), "OK"));
    }

    @GetMapping("/manifests/{manifestId}")
    public ResponseEntity<ApiResponse<ManifestResponse>> getManifest(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getManifest(manifestId), "OK"));
    }

    @GetMapping("/manifests/{manifestId}/levels")
    public ResponseEntity<ApiResponse<List<LevelResponse>>> listLevels(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listLevels(manifestId), "OK"));
    }

    @GetMapping("/manifests/{manifestId}/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> listSections(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSections(manifestId), "OK"));
    }

    @GetMapping("/manifests/{manifestId}/price-levels")
    public ResponseEntity<ApiResponse<List<PriceLevelResponse>>> listPriceLevels(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listPriceLevels(manifestId), "OK"));
    }

    @GetMapping("/sections/{sectionId}/rows")
    public ResponseEntity<ApiResponse<List<SeatRowResponse>>> listSeatRows(@PathVariable String sectionId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSeatRows(sectionId), "OK"));
    }

    @GetMapping("/rows/{rowId}/seats")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> listSeats(@PathVariable String rowId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSeats(rowId), "OK"));
    }
}
