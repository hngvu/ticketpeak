package io.qzz.hoangvu.ticketpeak.api.resale.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.CreateListingRequest;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.ResaleListingResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.ResaleOrderResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.service.ResaleListingService;
import io.qzz.hoangvu.ticketpeak.api.resale.service.ResaleOrderService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resale/listings")
@RequiredArgsConstructor
public class ResaleListingController {

    private final ResaleListingService listingService;
    private final ResaleOrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ResaleListingResponse>>> browseListings(
            @RequestParam @NotNull UUID eventId,
            Pageable pageable
    ) {
        Page<ResaleListingResponse> listings = listingService.browseListings(eventId, pageable);
        return ResponseEntity.ok(ApiResponse.success(listings, "OK"));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ResaleListingResponse>>> getMyListings(
            @AuthenticationPrincipal AuthenticatedAccount account,
            Pageable pageable
    ) {
        Page<ResaleListingResponse> listings = listingService.getMyListings(account.accountId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(listings, "OK"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResaleListingResponse>> createListing(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody CreateListingRequest request
    ) {
        ResaleListingResponse response = listingService.createListing(account.accountId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Listing created successfully"));
    }

    @DeleteMapping("/{listingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResaleListingResponse>> cancelListing(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID listingId
    ) {
        ResaleListingResponse response = listingService.cancelListing(account.accountId(), listingId);
        return ResponseEntity.ok(ApiResponse.success(response, "Listing cancelled successfully"));
    }

    @PostMapping("/{listingId}/purchase")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResaleOrderResponse>> initiatePurchase(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID listingId
    ) {
        ResaleOrderResponse response = orderService.initiatePurchase(account.accountId(), listingId);
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase initiated successfully"));
    }
}
