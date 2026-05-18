package io.qzz.hoangvu.ticketpeak.api.iam.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.CreatePermissionRequest;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.PermissionResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;
import io.qzz.hoangvu.ticketpeak.api.iam.service.PermissionService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request,
            Authentication authentication
    ) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        PermissionResponse response = permissionService.createPermission(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Permission created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions(
            @RequestParam(required = false) PermissionScope scope
    ) {
        List<PermissionResponse> response = permissionService.getAllPermissions(scope);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }
}
