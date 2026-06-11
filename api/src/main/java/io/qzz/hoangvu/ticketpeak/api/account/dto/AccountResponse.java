package io.qzz.hoangvu.ticketpeak.api.account.dto;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.model.Gender;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String avatarUrl,
        LocalDate birthDate,
        Gender gender,
        Integer cityId,
        String countryCode,
        Set<Role> roles,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getFirstName(),
                account.getLastName(),
                account.getAvatarUrl(),
                account.getBirthDate(),
                account.getGender(),
                account.getCityId(),
                account.getCountryCode(),
                account.getRoles(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
