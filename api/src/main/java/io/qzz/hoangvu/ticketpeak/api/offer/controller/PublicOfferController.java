package io.qzz.hoangvu.ticketpeak.api.offer.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.OfferResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.service.OfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/offers")
public class PublicOfferController {

    private final OfferService offerService;

    public PublicOfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OfferResponse>>> listEventOffers(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ApiResponse.success(offerService.listPublishedEventOffers(eventId), "OK"));
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<ApiResponse<OfferResponse>> getEventOffer(
            @PathVariable UUID eventId,
            @PathVariable UUID offerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(offerService.getEventOffer(eventId, offerId), "OK"));
    }
}
