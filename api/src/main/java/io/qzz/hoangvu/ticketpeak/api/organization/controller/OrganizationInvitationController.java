package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateInvitationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.InvitationDetailsResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.InvitationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.TokenRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationInvitationService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationInvitationController {

    private final OrganizationInvitationService invitationService;

    public OrganizationInvitationController(OrganizationInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/{orgId}/invitations")
    public ResponseEntity<ApiResponse<InvitationResponse>> createInvitation(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        InvitationResponse response = invitationService.createInvitation(orgId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Invitation created"));
    }

    @GetMapping("/{orgId}/invitations")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getOrganizationInvitations(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        List<InvitationResponse> response = invitationService.getOrganizationInvitations(orgId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/invitations/me")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations(
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        List<InvitationResponse> response = invitationService.getMyInvitations(principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/invitations/validate")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> validateToken(
            @RequestParam String token
    ) {
        InvitationDetailsResponse response = invitationService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @Valid @RequestBody TokenRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        invitationService.acceptInvitation(request.token(), principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation accepted"));
    }

    @PostMapping("/invitations/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @Valid @RequestBody TokenRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        invitationService.rejectInvitation(request.token(), principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation rejected"));
    }
}
