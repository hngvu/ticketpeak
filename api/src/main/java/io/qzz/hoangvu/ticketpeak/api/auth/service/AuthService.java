package io.qzz.hoangvu.ticketpeak.api.auth.service;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.LogoutRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.LoginRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.RefreshRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.TokenPairResponse;
import io.qzz.hoangvu.ticketpeak.api.account.exception.AccountException;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.auth.exception.AuthException;
import io.qzz.hoangvu.ticketpeak.api.security.JwtService;
import io.qzz.hoangvu.ticketpeak.api.security.JwtTokenPair;
import io.qzz.hoangvu.ticketpeak.api.security.RefreshTokenStore;
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

        JwtTokenPair tokenPair = jwtService.issueTokenPair(account.getId(), account.getRoles());
        refreshTokenStore.store(account.getId(), tokenPair.refreshTokenId());
        return TokenPairResponse.from(tokenPair, jwtService.accessTokenExpirySeconds(), jwtService.refreshTokenExpirySeconds());
    }

    @Transactional
    public TokenPairResponse refresh(RefreshRequest request) {
        JwtService.ParsedRefreshToken parsedToken;
        try {
            parsedToken = jwtService.parseRefreshToken(request.refreshToken());
        } catch (RuntimeException exception) {
            throw AuthException.invalidRefreshToken();
        }
        Account account = loadActiveAccount(parsedToken.accountId());
        if (!refreshTokenStore.matches(account.getId(), parsedToken.refreshTokenId())) {
            throw AuthException.refreshTokenRevoked();
        }

        JwtTokenPair tokenPair = jwtService.issueTokenPair(account.getId(), account.getRoles());
        refreshTokenStore.store(account.getId(), tokenPair.refreshTokenId());
        return TokenPairResponse.from(tokenPair, jwtService.accessTokenExpirySeconds(), jwtService.refreshTokenExpirySeconds());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        JwtService.ParsedRefreshToken parsedToken;
        try {
            parsedToken = jwtService.parseRefreshToken(request.refreshToken());
        } catch (RuntimeException exception) {
            throw AuthException.invalidRefreshToken();
        }
        if (!refreshTokenStore.matches(parsedToken.accountId(), parsedToken.refreshTokenId())) {
            throw AuthException.refreshTokenRevoked();
        }

        refreshTokenStore.delete(parsedToken.accountId());
    }

    private Account loadActiveAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountException.notFound());
        verifyActiveAccount(account);
        return account;
    }

    private void verifyActiveAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw AuthException.accountInactive();
        }
    }

    private ApiException invalidCredentials(String message) {
        return AuthException.invalidCredentials(message);
    }
}
