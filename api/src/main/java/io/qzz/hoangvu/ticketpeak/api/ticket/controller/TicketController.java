package io.qzz.hoangvu.ticketpeak.api.ticket.controller;

import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.InitiateTransferRequest;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.TicketQrResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.TicketResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.TransferResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> listMyTickets(
            @AuthenticationPrincipal AuthenticatedAccount account,
            Pageable pageable) {
        Page<TicketResponse> responses = ticketService.listMyTickets(account.accountId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) {
        TicketResponse response = ticketService.getMyTicket(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<ApiResponse<TicketQrResponse>> getQr(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) {
        TicketQrResponse response = ticketService.getQr(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/transfers")
    public ResponseEntity<ApiResponse<TransferResponse>> initiateTransfer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id,
            @RequestBody @Valid InitiateTransferRequest request) {
        TransferResponse response = ticketService.initiateTransfer(account.accountId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/transfers/{transferId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptTransfer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID transferId) {
        ticketService.acceptTransfer(account.accountId(), transferId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/transfers/{transferId}")
    public ResponseEntity<ApiResponse<Void>> cancelTransfer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID transferId) {
        ticketService.cancelTransfer(account.accountId(), transferId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
