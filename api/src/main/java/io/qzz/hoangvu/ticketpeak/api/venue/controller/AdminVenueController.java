package io.qzz.hoangvu.ticketpeak.api.venue.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.*;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/venues")
public class AdminVenueController {

    private final VenueService venueService;

    public AdminVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // ======================== VENUE ========================

    @PostMapping
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createVenue(req), "Venue created"));
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

    @PostMapping("/manifests/{manifestId}/publish")
    public ResponseEntity<ApiResponse<ManifestResponse>> publishManifest(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.publishManifest(manifestId), "Manifest published"));
    }

    @PostMapping("/manifests/{manifestId}/archive")
    public ResponseEntity<ApiResponse<ManifestResponse>> archiveManifest(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.archiveManifest(manifestId), "Manifest archived"));
    }

    @PostMapping("/manifests/{manifestId}/clone")
    public ResponseEntity<ApiResponse<ManifestResponse>> cloneManifest(
            @PathVariable String manifestId, @RequestBody(required = false) CloneManifestRequest req) {
        CloneManifestRequest safeReq = req != null ? req : new CloneManifestRequest(null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.cloneManifest(manifestId, safeReq), "Manifest cloned"));
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
            @PathVariable String manifestId, @Valid @RequestBody UpsertLookupRequest req) {
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

    // ======================== AREAS ========================

    @PostMapping("/manifests/{manifestId}/ga-areas")
    public ResponseEntity<ApiResponse<GAAreaResponse>> createGAArea(
            @PathVariable String manifestId, @Valid @RequestBody CreateGAAreaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createGAArea(manifestId, req), "GA area created"));
    }

    @GetMapping("/manifests/{manifestId}/ga-areas")
    public ResponseEntity<ApiResponse<List<GAAreaResponse>>> listGAAreas(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listGAAreas(manifestId), "OK"));
    }

    @PostMapping("/manifests/{manifestId}/rs-areas")
    public ResponseEntity<ApiResponse<RSAreaResponse>> createRSArea(
            @PathVariable String manifestId, @Valid @RequestBody CreateRSAreaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createRSArea(manifestId, req), "RS area created"));
    }

    @GetMapping("/manifests/{manifestId}/rs-areas")
    public ResponseEntity<ApiResponse<List<RSAreaResponse>>> listRSAreas(@PathVariable String manifestId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listRSAreas(manifestId), "OK"));
    }

    // ======================== SEAT ROWS & SEATS ========================

    @PostMapping("/rs-areas/{rsAreaId}/rows")
    public ResponseEntity<ApiResponse<SeatRowResponse>> createSeatRow(
            @PathVariable String rsAreaId, @Valid @RequestBody CreateSeatRowRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(venueService.createSeatRow(rsAreaId, req), "Seat row created"));
    }

    @GetMapping("/rs-areas/{rsAreaId}/rows")
    public ResponseEntity<ApiResponse<List<SeatRowResponse>>> listSeatRows(@PathVariable String rsAreaId) {
        return ResponseEntity.ok(ApiResponse.success(venueService.listSeatRows(rsAreaId), "OK"));
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
