package io.qzz.hoangvu.ticketpeak.api.reservation.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.reservation.dto.CreateReservationRequest;
import io.qzz.hoangvu.ticketpeak.api.reservation.dto.ReservationResponse;
import io.qzz.hoangvu.ticketpeak.api.reservation.service.ReservationService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> createReservation(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return ApiResponse.success(
                reservationService.createReservation(account.accountId(), request),
                "Reservation created"
        );
    }

    @GetMapping
    public ApiResponse<List<ReservationResponse>> listReservations(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        return ApiResponse.success(reservationService.listReservations(account.accountId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservation(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(reservationService.getReservation(account.accountId(), id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<ReservationResponse> confirmReservation(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(
                reservationService.confirmReservation(account.accountId(), id),
                "Reservation confirmed"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<ReservationResponse> cancelReservation(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(
                reservationService.cancelReservation(account.accountId(), id),
                "Reservation cancelled"
        );
    }
}
