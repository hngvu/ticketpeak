package io.qzz.hoangvu.ticketpeak.api.ticket.controller;

import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.CheckInRequest;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.CheckInResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/partner/events/{eventId}/check-in")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @AuthenticationPrincipal AuthenticatedAccount staff,
            @PathVariable UUID eventId,
            @RequestBody @Valid CheckInRequest request) {
        CheckInResponse result = checkInService.checkIn(staff.accountId(), eventId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<java.util.List<io.qzz.hoangvu.ticketpeak.api.ticket.dto.SimulatedTicketResponse>>> getTicketsForSimulation(
            @PathVariable UUID eventId) {
        java.util.List<io.qzz.hoangvu.ticketpeak.api.ticket.dto.SimulatedTicketResponse> responses = checkInService.getTicketsForSimulation(eventId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
