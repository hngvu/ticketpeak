package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitationStatus;
import java.time.Instant;

public record InvitationDetailsResponse(
        String orgName,
        String inviterName,
        OrganizationInvitationStatus status,
        Instant expiresAt
) {
}
