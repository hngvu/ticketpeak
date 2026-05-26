package io.qzz.hoangvu.ticketpeak.api.payout.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.CreatePayoutMethodRequest;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutMethodResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutMethodService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payout/methods")
@RequiredArgsConstructor
public class PayoutMethodController {

    private final PayoutMethodService payoutMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayoutMethodResponse>>> listMethods(
            @AuthenticationPrincipal AuthenticatedAccount account) {
        List<PayoutMethodResponse> responses = payoutMethodService.listMethods(account.accountId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PayoutMethodResponse>> addMethod(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestBody @Valid CreatePayoutMethodRequest request) {
        PayoutMethodResponse response = payoutMethodService.addMethod(account.accountId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<Void>> setPrimary(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) {
        payoutMethodService.setPrimary(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeMethod(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) {
        payoutMethodService.removeMethod(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
