package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.AttractionResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.CreateAttractionRequest;
import io.qzz.hoangvu.ticketpeak.api.event.service.AttractionService;
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
@RequestMapping("/api/ops/attractions")
public class OpsAttractionController {

    private final AttractionService attractionService;

    public OpsAttractionController(AttractionService attractionService) {
        this.attractionService = attractionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttractionResponse>> createAttraction(
            @Valid @RequestBody CreateAttractionRequest request,
            Authentication authentication
    ) {
        verifyAdmin(authentication);
        AttractionResponse response = attractionService.createAttraction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Attraction created successfully"));
    }

    private void verifyAdmin(Authentication authentication) {
        AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can perform this action");
        }
    }
}
