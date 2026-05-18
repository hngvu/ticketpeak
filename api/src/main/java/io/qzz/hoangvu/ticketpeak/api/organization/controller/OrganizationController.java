package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.UpdateOrganizationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationBySlug(
            @PathVariable String slug
    ) {
        OrganizationResponse response = organizationService.getOrganizationBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationResponse response = organizationService.updateOrganization(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization updated"));
    }
}
