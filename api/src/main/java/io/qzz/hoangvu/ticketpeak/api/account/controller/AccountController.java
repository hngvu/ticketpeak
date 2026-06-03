package io.qzz.hoangvu.ticketpeak.api.account.controller;

import io.qzz.hoangvu.ticketpeak.api.account.dto.AccountResponse;
import io.qzz.hoangvu.ticketpeak.api.account.dto.RegisterAccountRequest;
import io.qzz.hoangvu.ticketpeak.api.account.dto.UpdateAccountRequest;
import io.qzz.hoangvu.ticketpeak.api.account.service.AccountService;
import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public class AccountController {

    // ── /api/ops/accounts ────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/ops/accounts")
    static class OpsAccountController {

        private final AccountService accountService;

        OpsAccountController(AccountService accountService) {
            this.accountService = accountService;
        }

        /**
         * List / search accounts.
         *
         * @param q    optional free-text filter on email or full name
         * @param role optional role filter (BUYER | ORGANIZER | ADMIN)
         */
        @GetMapping
        public ResponseEntity<ApiResponse<List<AccountResponse>>> listAccounts(
                @RequestParam(required = false) String q,
                @RequestParam(required = false) Role role
        ) {
            return ResponseEntity.ok(ApiResponse.success(accountService.listAccounts(q, role), "OK"));
        }
    }

    // ── /api/accounts ─────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/accounts")
    static class PublicAccountController {

        private final AccountService accountService;

        PublicAccountController(AccountService accountService) {
            this.accountService = accountService;
        }

        @GetMapping("/exists")
        public ResponseEntity<ApiResponse<Boolean>> existsByEmail(@RequestParam String email) {
            return ResponseEntity.ok(ApiResponse.success(accountService.existsByEmail(email), "OK"));
        }

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<AccountResponse>> register(
                @Valid @RequestBody RegisterAccountRequest request
        ) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(accountService.register(request), "Account created"));
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse<AccountResponse>> me(Authentication authentication) {
            AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
            return ResponseEntity.ok(ApiResponse.success(accountService.me(principal.accountId()), "OK"));
        }

        @PatchMapping("/me")
        public ResponseEntity<ApiResponse<AccountResponse>> updateMe(
                Authentication authentication,
                @Valid @RequestBody UpdateAccountRequest request
        ) {
            AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
            return ResponseEntity.ok(ApiResponse.success(
                    accountService.updateMe(principal.accountId(), request), "Profile updated"));
        }
    }
}
