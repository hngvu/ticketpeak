package io.qzz.hoangvu.ticketpeak.api.account.controller;

import io.qzz.hoangvu.ticketpeak.api.account.dto.AccountResponse;
import io.qzz.hoangvu.ticketpeak.api.account.dto.RegisterAccountRequest;
import io.qzz.hoangvu.ticketpeak.api.account.dto.UpdateAccountRequest;
import io.qzz.hoangvu.ticketpeak.api.account.service.AccountService;
import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AccountResponse>> register(@Valid @RequestBody RegisterAccountRequest request) {
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
        return ResponseEntity.ok(ApiResponse.success(accountService.updateMe(principal.accountId(), request), "Profile updated"));
    }
}
