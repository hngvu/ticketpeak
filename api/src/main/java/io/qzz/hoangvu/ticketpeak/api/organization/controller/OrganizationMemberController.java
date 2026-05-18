package io.qzz.hoangvu.ticketpeak.api.organization.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.MemberRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationMemberResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.service.OrganizationMemberService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationMemberController {

    private final OrganizationMemberService memberService;

    public OrganizationMemberController(OrganizationMemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{orgId}/members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(
            @PathVariable UUID orgId,
            @RequestParam(required = false) OrganizationMemberStatus status,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        List<OrganizationMemberResponse> response = memberService.getOrganizationMembers(orgId, status, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{orgId}/members/{accountId}")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getMemberStatus(
            @PathVariable UUID orgId,
            @PathVariable UUID accountId,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationMemberResponse response = memberService.getMemberStatus(orgId, accountId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @DeleteMapping("/{orgId}/members/{accountId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID orgId,
            @PathVariable UUID accountId,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        memberService.removeMember(orgId, accountId, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed"));
    }

    @PostMapping("/{orgId}/members/remove")
    public ResponseEntity<ApiResponse<Void>> removeMemberWithBody(
            @PathVariable UUID orgId,
            @Valid @RequestBody MemberRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        memberService.removeMember(orgId, request.accountId(), principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed"));
    }

    @PostMapping("/{orgId}/members/leave")
    public ResponseEntity<ApiResponse<Void>> leaveOrganization(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        memberService.leaveOrganization(orgId, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Left organization"));
    }

    @PostMapping("/{orgId}/members/{accountId}/restore")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> restoreMember(
            @PathVariable UUID orgId,
            @PathVariable UUID accountId,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationMemberResponse response = memberService.restoreMember(orgId, accountId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Member restored"));
    }

    @PostMapping("/{orgId}/members/restore")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> restoreMemberWithBody(
            @PathVariable UUID orgId,
            @Valid @RequestBody MemberRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        OrganizationMemberResponse response = memberService.restoreMember(orgId, request.accountId(), principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Member restored"));
    }
}
