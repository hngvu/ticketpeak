package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;

import java.time.Instant;
import java.util.UUID;

public record OrganizationMemberResponse(
        Integer id,
        UUID organizationId,
        UUID accountId,
        MemberAccountSummary account,
        OrganizationMemberStatus status,
        UUID invitedBy,
        Instant joinedAt,
        Instant updatedAt
) {
    public static OrganizationMemberResponse from(OrganizationMember member, MemberAccountSummary accountSummary) {
        return new OrganizationMemberResponse(
                member.getId(),
                member.getOrganization().getId(),
                member.getAccountId(),
                accountSummary,
                member.getStatus(),
                member.getInvitedBy(),
                member.getJoinedAt(),
                member.getUpdatedAt()
        );
    }
}
