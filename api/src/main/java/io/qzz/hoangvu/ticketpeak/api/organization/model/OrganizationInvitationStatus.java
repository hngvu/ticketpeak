package io.qzz.hoangvu.ticketpeak.api.organization.model;

public enum OrganizationInvitationStatus {
    PENDING,   // Sent, waiting for response
    ACCEPTED,  // Invitee accepted → OrganizationMember record created
    REJECTED,  // Invitee declined
    EXPIRED    // Passed expiresAt without response
}
