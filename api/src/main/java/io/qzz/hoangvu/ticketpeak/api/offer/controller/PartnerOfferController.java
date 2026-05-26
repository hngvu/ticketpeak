package io.qzz.hoangvu.ticketpeak.api.offer.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.CreateOfferRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.OfferResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.UpdateOfferRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/partner/events/{eventId}/offers")
public class PartnerOfferController {

    private final OfferService offerService;

    public PartnerOfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateOfferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(offerService.createOffer(eventId, request), "Offer created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<OfferResponse>>> listEventOffers(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(ApiResponse.success(offerService.listEventOffers(eventId), "OK"));
    }

    @PutMapping("/{offerId}")
    public ResponseEntity<ApiResponse<OfferResponse>> updateOffer(
            @PathVariable UUID eventId,
            @PathVariable UUID offerId,
            @Valid @RequestBody UpdateOfferRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                offerService.updateOffer(eventId, offerId, request), "Offer updated"));
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<ApiResponse<OfferResponse>> getEventOfferForPartner(
            @PathVariable UUID eventId,
            @PathVariable UUID offerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                offerService.getEventOfferForPartner(eventId, offerId), "OK"));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<ApiResponse<Void>> deleteOffer(
            @PathVariable UUID eventId,
            @PathVariable UUID offerId
    ) {
        offerService.deleteOffer(eventId, offerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Offer deleted"));
    }
}
