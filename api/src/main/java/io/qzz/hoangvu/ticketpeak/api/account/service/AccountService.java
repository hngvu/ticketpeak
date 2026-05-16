package io.qzz.hoangvu.ticketpeak.api.account.service;

import io.qzz.hoangvu.ticketpeak.api.account.dto.AccountResponse;
import io.qzz.hoangvu.ticketpeak.api.account.dto.RegisterAccountRequest;
import io.qzz.hoangvu.ticketpeak.api.account.dto.UpdateAccountRequest;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.model.Gender;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse register(RegisterAccountRequest request) {
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email is already registered");
        }

        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .firstName(trimToNull(request.firstName()))
                .lastName(trimToNull(request.lastName()))
                .avatarUrl(trimToNull(request.avatarUrl()))
                .birthDate(request.birthDate())
                .gender(parseGender(request.gender()))
                .cityId(request.cityId())
                .countryCode(trimToNull(request.countryCode()))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build();

        return AccountResponse.from(accountRepository.save(account));
    }

    public AccountResponse me(UUID accountId) {
        return AccountResponse.from(loadAccount(accountId));
    }

    @Transactional
    public AccountResponse updateMe(UUID accountId, UpdateAccountRequest request) {
        Account account = loadAccount(accountId);
        applyUpdate(account, request);
        return AccountResponse.from(accountRepository.save(account));
    }

    private Account loadAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account does not exist"));
    }

    private void applyUpdate(Account account, UpdateAccountRequest request) {
        if (request.firstName() != null) {
            account.setFirstName(trimToNull(request.firstName()));
        }
        if (request.lastName() != null) {
            account.setLastName(trimToNull(request.lastName()));
        }
        if (request.avatarUrl() != null) {
            account.setAvatarUrl(trimToNull(request.avatarUrl()));
        }
        if (request.birthDate() != null) {
            account.setBirthDate(request.birthDate());
        }
        if (request.gender() != null) {
            account.setGender(parseGender(request.gender()));
        }
        if (request.cityId() != null) {
            account.setCityId(request.cityId());
        }
        if (request.countryCode() != null) {
            account.setCountryCode(trimToNull(request.countryCode()));
        }
    }

    private Gender parseGender(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Gender.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
