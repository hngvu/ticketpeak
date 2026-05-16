package io.qzz.hoangvu.ticketpeak.api.auth.service;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.LogoutRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.LoginRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.RefreshRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.TokenPairResponse;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.security.JwtService;
import io.qzz.hoangvu.ticketpeak.api.security.JwtTokenPair;
import io.qzz.hoangvu.ticketpeak.api.security.RefreshTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenStore refreshTokenStore
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Transactional
    public TokenPairResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> invalidCredentials("Invalid email or password"));

        verifyActiveAccount(account);
        if (account.getPassword() == null || !passwordEncoder.matches(request.password(), account.getPassword())) {
            throw invalidCredentials("Invalid email or password");
        }

        JwtTokenPair tokenPair = jwtService.issueTokenPair(account.getId(), account.getRole());
        refreshTokenStore.store(account.getId(), tokenPair.refreshTokenId());
        return TokenPairResponse.from(tokenPair, jwtService.accessTokenExpirySeconds(), jwtService.refreshTokenExpirySeconds());
    }

    @Transactional
    public TokenPairResponse refresh(RefreshRequest request) {
        JwtService.ParsedRefreshToken parsedToken;
        try {
            parsedToken = jwtService.parseRefreshToken(request.refreshToken());
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid");
        }
        Account account = loadActiveAccount(parsedToken.accountId());
        if (!refreshTokenStore.matches(account.getId(), parsedToken.refreshTokenId())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REVOKED", "Refresh token is no longer valid");
        }

        JwtTokenPair tokenPair = jwtService.issueTokenPair(account.getId(), account.getRole());
        refreshTokenStore.store(account.getId(), tokenPair.refreshTokenId());
        return TokenPairResponse.from(tokenPair, jwtService.accessTokenExpirySeconds(), jwtService.refreshTokenExpirySeconds());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        JwtService.ParsedRefreshToken parsedToken;
        try {
            parsedToken = jwtService.parseRefreshToken(request.refreshToken());
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid");
        }
        if (!refreshTokenStore.matches(parsedToken.accountId(), parsedToken.refreshTokenId())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REVOKED", "Refresh token is no longer valid");
        }

        refreshTokenStore.delete(parsedToken.accountId());
    }

    private Account loadActiveAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account does not exist"));
        verifyActiveAccount(account);
        return account;
    }

    private void verifyActiveAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", "Account is not active");
        }
    }

    private ApiException invalidCredentials(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", message);
    }
}
