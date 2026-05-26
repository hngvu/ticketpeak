package io.qzz.hoangvu.ticketpeak.api.offer.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.CreateTicketTypeRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.TicketTypeResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.service.TicketTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/partner/events/{eventId}/ticket-types")
public class PartnerTicketTypeController {

    private final TicketTypeService ticketTypeService;

    public PartnerTicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketTypeResponse>> createTicketType(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateTicketTypeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ticketTypeService.createTicketType(eventId, request), "Ticket type created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<TicketTypeResponse>>> listEventTicketTypes(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.listEventTicketTypes(eventId), "OK"));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> getEventTicketTypeForPartner(
            @PathVariable UUID eventId,
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                ticketTypeService.getEventTicketTypeForPartner(eventId, slug), "OK"));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<ApiResponse<Void>> deleteTicketType(
            @PathVariable UUID eventId,
            @PathVariable String slug
    ) {
        ticketTypeService.deleteTicketType(eventId, slug);
        return ResponseEntity.ok(ApiResponse.success(null, "Ticket type deleted"));
    }
}
