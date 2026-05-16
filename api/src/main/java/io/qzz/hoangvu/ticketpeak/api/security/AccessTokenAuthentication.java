package io.qzz.hoangvu.ticketpeak.api.security;

import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;

import java.util.UUID;

public record AccessTokenAuthentication(
        UUID accountId,
        Role role,
        String email
) {
}
