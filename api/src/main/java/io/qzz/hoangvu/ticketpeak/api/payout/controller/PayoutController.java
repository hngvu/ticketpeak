package io.qzz.hoangvu.ticketpeak.api.payout.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> listMyPayouts(
            @AuthenticationPrincipal AuthenticatedAccount account,
            Pageable pageable) {
        Page<PayoutResponse> responses = payoutService.listPayoutsForSeller(account.accountId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
