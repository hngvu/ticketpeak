package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.*;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationInvitationService;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationMemberService;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partner/organizations")
public class PartnerOrganizationController {

    private final OrganizationService organizationService;
    private final OrganizationMemberService memberService;
    private final OrganizationInvitationService invitationService;

    public PartnerOrganizationController(
            OrganizationService organizationService,
            OrganizationMemberService memberService,
            OrganizationInvitationService invitationService
    ) {
        this.organizationService = organizationService;
        this.memberService = memberService;
        this.invitationService = invitationService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization updated"));
    }

    // ======================== MEMBERS ========================

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getOrganizationMembers(
            @PathVariable UUID id,
            @RequestParam(required = false) OrganizationMemberStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getOrganizationMembers(id, status), "OK"));
    }

    @GetMapping("/{id}/members/{accountId}")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getMemberStatus(
            @PathVariable UUID id,
            @PathVariable UUID accountId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberStatus(id, accountId), "OK"));
    }

    @DeleteMapping("/{id}/members/{accountId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID accountId
    ) {
        memberService.removeMember(id, accountId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveOrganization(@PathVariable UUID id) {
        memberService.leaveOrganization(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Successfully left the organization"));
    }

    @PostMapping("/{id}/members/{accountId}/restore")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> restoreMember(
            @PathVariable UUID id,
            @PathVariable UUID accountId
    ) {
        OrganizationMemberResponse response = memberService.restoreMember(id, accountId);
        return ResponseEntity.ok(ApiResponse.success(response, "Member restored successfully"));
    }

    // ======================== INVITATIONS ========================

    @PostMapping("/{id}/invitations")
    public ResponseEntity<ApiResponse<InvitationResponse>> inviteMember(
            @PathVariable UUID id,
            @Valid @RequestBody CreateInvitationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invitationService.createInvitation(id, request), "Invitation sent"));
    }

    @GetMapping("/{id}/invitations")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getOrganizationInvitations(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getOrganizationInvitations(id), "OK"));
    }

    @GetMapping("/invitations/my")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations() {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getMyInvitations(), "OK"));
    }

    @GetMapping("/invitations/validate")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> validateInvitationToken(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(invitationService.validateToken(token), "Token is valid"));
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(@RequestParam String token) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation accepted"));
    }

    @PostMapping("/invitations/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(@RequestParam String token) {
        invitationService.rejectInvitation(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation rejected"));
    }
}
