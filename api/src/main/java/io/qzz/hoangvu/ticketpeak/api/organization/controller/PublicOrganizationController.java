package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class PublicOrganizationController {

    private final OrganizationService organizationService;

    public PublicOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganization(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getOrganization(id), "OK"));
    }
}
