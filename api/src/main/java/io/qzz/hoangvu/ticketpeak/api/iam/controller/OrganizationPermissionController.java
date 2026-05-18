package io.qzz.hoangvu.ticketpeak.api.iam.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.AccountPermissionResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.GrantPermissionRequest;
import io.qzz.hoangvu.ticketpeak.api.iam.service.PermissionService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{orgId}/permissions")
public class OrganizationPermissionController {

    private final PermissionService permissionService;

    public OrganizationPermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountPermissionResponse>> grantPermission(
            @PathVariable UUID orgId,
            @Valid @RequestBody GrantPermissionRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        AccountPermissionResponse response = permissionService.grantPermission(orgId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Permission granted"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable UUID orgId,
            @RequestParam UUID accountId,
            @RequestParam String permissionCode,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        permissionService.revokePermission(orgId, accountId, permissionCode, principal);
        return ResponseEntity.ok(ApiResponse.message("Permission revoked"));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<List<AccountPermissionResponse>>> getAccountPermissions(
            @PathVariable UUID orgId,
            @PathVariable UUID accountId,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        List<AccountPermissionResponse> response = permissionService.getAccountPermissions(orgId, accountId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }
}
