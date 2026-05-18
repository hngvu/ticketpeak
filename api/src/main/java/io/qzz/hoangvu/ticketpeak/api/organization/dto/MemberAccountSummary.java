package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;

import java.util.UUID;

public record MemberAccountSummary(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String avatarUrl
) {
    public static MemberAccountSummary from(Account account) {
        if (account == null) {
            return null;
        }
        return new MemberAccountSummary(
                account.getId(),
                account.getEmail(),
                account.getFirstName(),
                account.getLastName(),
                account.getAvatarUrl()
        );
    }
}
