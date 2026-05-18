package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationSearchParams;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.UpdateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/organizations")
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    public AdminOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationResponse response = organizationService.createOrganization(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Organization created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrganizationResponse>>> searchOrganizations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) OrganizationStatus status,
            Pageable pageable,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationSearchParams params = new OrganizationSearchParams(name, status);
        Page<OrganizationResponse> response = organizationService.searchOrganizations(params, pageable, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationResponse response = organizationService.getOrganizationById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationResponse response = organizationService.adminUpdateOrganization(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization updated"));
    }
}
