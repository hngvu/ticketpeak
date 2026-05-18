package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitation;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        Integer id,
        UUID organizationId,
        UUID inviteeAccountId,
        UUID invitedBy,
        String token,
        OrganizationInvitationStatus status,
        Instant expiresAt,
        Instant respondedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static InvitationResponse from(OrganizationInvitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getOrganization().getId(),
                invitation.getInviteeAccountId(),
                invitation.getInvitedBy(),
                invitation.getToken(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getRespondedAt(),
                invitation.getCreatedAt(),
                invitation.getUpdatedAt()
        );
    }
}
