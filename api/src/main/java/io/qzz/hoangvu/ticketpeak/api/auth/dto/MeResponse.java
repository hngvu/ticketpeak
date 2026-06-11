package io.qzz.hoangvu.ticketpeak.api.auth.dto;

import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;

import java.util.Set;
import java.util.UUID;

public record MeResponse(
        UUID accountId,
        String email,
        Set<Role> roles,
        String firstName,
        String lastName,
        String avatarUrl,
        AccountStatus status
) {
}
