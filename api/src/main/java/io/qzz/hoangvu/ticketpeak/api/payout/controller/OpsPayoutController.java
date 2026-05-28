package io.qzz.hoangvu.ticketpeak.api.payout.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.UpdatePayoutStatusRequest;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutStatus;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/ops/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OpsPayoutController {

    private final PayoutService payoutService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> listPayouts(
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(required = false) Instant scheduledBefore,
            Pageable pageable) {
        Page<PayoutResponse> responses = payoutService.listPayoutsForAdmin(status, scheduledBefore, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayoutResponse>> getPayout(@PathVariable UUID id) {
        PayoutResponse response = payoutService.getPayoutForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PayoutResponse>> updatePayoutStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePayoutStatusRequest request) {
        PayoutResponse response = payoutService.updatePayoutStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
