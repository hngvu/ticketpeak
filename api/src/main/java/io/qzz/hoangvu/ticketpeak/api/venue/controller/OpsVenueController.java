package io.qzz.hoangvu.ticketpeak.api.venue.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.*;
import io.qzz.hoangvu.ticketpeak.api.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ops/venues")
@PreAuthorize("hasRole('ADMIN')")
public class OpsVenueController {

    private final VenueService venueService;

    public OpsVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // ======================== VENUE ========================

    @PostMapping
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createVenue(req), "Venue created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(@PathVariable UUID id, @Valid @RequestBody UpdateVenueRequest req) {
        return ResponseEntity.ok(ApiResponse.success(venueService.updateVenue(id, req), "Venue updated"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<VenueResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.activateVenue(id), "Venue activated"));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<VenueResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.deactivateVenue(id), "Venue deactivated"));
    }

    // ======================== MANIFEST ========================

    @PostMapping("/{venueId}/manifests")
    public ResponseEntity<ApiResponse<ManifestResponse>> createManifest(
            @PathVariable UUID venueId, @Valid @RequestBody CreateManifestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createManifest(venueId, req), "Manifest created"));
    }

    @GetMapping("/{venueId}/manifests")
    public ResponseEntity<ApiResponse<List<ManifestResponse>>> listManifests(@PathVariable UUID venueId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listManifests(venueId), "OK"));
    }

    @GetMapping("/manifests/{manifestId}")
    public ResponseEntity<ApiResponse<ManifestResponse>> getManifest(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getManifest(manifestId), "OK"));
    }

    @PutMapping("/manifests/{manifestId}")
    public ResponseEntity<ApiResponse<ManifestResponse>> updateManifest(
            @PathVariable String manifestId, @Valid @RequestBody UpdateManifestRequest req) {
        return ResponseEntity.ok(ApiResponse.success(venueService.updateManifest(manifestId, req), "Manifest updated"));
    }

    @PostMapping("/manifests/{manifestId}/publish")
    public ResponseEntity<ApiResponse<ManifestResponse>> publishManifest(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.publishManifest(manifestId), "Manifest published"));
    }

    @PostMapping("/manifests/{manifestId}/archive")
    public ResponseEntity<ApiResponse<ManifestResponse>> archiveManifest(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.archiveManifest(manifestId), "Manifest archived"));
    }


    // ======================== LOOKUP TABLES ========================

    @PutMapping("/manifests/{manifestId}/levels")
    public ResponseEntity<ApiResponse<LevelResponse>> upsertLevel(
            @PathVariable String manifestId, @Valid @RequestBody UpsertLookupRequest req) {
        return ResponseEntity.ok(ApiResponse.success(venueService.upsertLevel(manifestId, req), "Level saved"));
    }

    @GetMapping("/manifests/{manifestId}/levels")
    public ResponseEntity<ApiResponse<List<LevelResponse>>> listLevels(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listLevels(manifestId), "OK"));
    }

    @PutMapping("/manifests/{manifestId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> upsertSection(
            @PathVariable String manifestId, @Valid @RequestBody UpsertSectionRequest req) {
        return ResponseEntity.ok(ApiResponse.success(venueService.upsertSection(manifestId, req), "Section saved"));
    }

    @GetMapping("/manifests/{manifestId}/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> listSections(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSections(manifestId), "OK"));
    }

    @PutMapping("/manifests/{manifestId}/price-levels")
    public ResponseEntity<ApiResponse<PriceLevelResponse>> upsertPriceLevel(
            @PathVariable String manifestId, @Valid @RequestBody UpsertLookupRequest req) {
        return ResponseEntity.ok(ApiResponse.success(venueService.upsertPriceLevel(manifestId, req), "Price level saved"));
    }

    @GetMapping("/manifests/{manifestId}/price-levels")
    public ResponseEntity<ApiResponse<List<PriceLevelResponse>>> listPriceLevels(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listPriceLevels(manifestId), "OK"));
    }

    // Removed GA/RS areas endpoints

    // ======================== SEAT ROWS & SEATS ========================

    @PostMapping("/sections/{sectionId}/rows")
    public ResponseEntity<ApiResponse<SeatRowResponse>> createSeatRow(
            @PathVariable String sectionId, @Valid @RequestBody CreateSeatRowRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createSeatRow(sectionId, req), "Seat row created"));
    }

    @GetMapping("/sections/{sectionId}/rows")
    public ResponseEntity<ApiResponse<List<SeatRowResponse>>> listSeatRows(@PathVariable String sectionId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSeatRows(sectionId), "OK"));
    }

    @PostMapping("/rows/{rowId}/seats")
    public ResponseEntity<ApiResponse<SeatResponse>> createSeat(
            @PathVariable String rowId, @Valid @RequestBody CreateSeatRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createSeat(rowId, req), "Seat created"));
    }

    @GetMapping("/rows/{rowId}/seats")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> listSeats(@PathVariable String rowId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSeats(rowId), "OK"));
    }
}
