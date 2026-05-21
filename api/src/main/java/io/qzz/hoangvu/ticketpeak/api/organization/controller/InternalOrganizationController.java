package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationSearchParams;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.UpdateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/organizations")
public class InternalOrganizationController {

    private final OrganizationService organizationService;

    public InternalOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Organization created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrganizationResponse>>> searchOrganizations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) OrganizationStatus status,
            Pageable pageable
    ) {
        OrganizationSearchParams params = new OrganizationSearchParams(name, status);
        Page<OrganizationResponse> response = organizationService.searchOrganizations(params, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable UUID id
    ) {
        OrganizationResponse response = organizationService.getOrganization(id);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.adminUpdateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization updated"));
    }
}
