package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.ClassificationResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.CreateClassificationRequest;
import io.qzz.hoangvu.ticketpeak.api.event.service.ClassificationService;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ops/classifications")
public class OpsClassificationController {

    private final ClassificationService classificationService;

    public OpsClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassificationResponse>> createClassification(
            @Valid @RequestBody CreateClassificationRequest request,
            Authentication authentication
    ) {
        verifyAdmin(authentication);
        ClassificationResponse response = classificationService.createClassification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Classification created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassificationResponse>> updateClassification(
            @PathVariable UUID id,
            @Valid @RequestBody CreateClassificationRequest request,
            Authentication authentication
    ) {
        verifyAdmin(authentication);
        ClassificationResponse response = classificationService.updateClassification(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Classification updated successfully"));
    }

    private void verifyAdmin(Authentication authentication) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can perform this action");
        }
    }
}
