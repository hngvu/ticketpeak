package io.qzz.hoangvu.ticketpeak.api.organization.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class OrganizationException {
    private OrganizationException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found");
    }

    public static ApiException invalidOwnerRole() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OWNER_ROLE", "Owner account must be an organizer or admin");
    }

    public static ApiException invalidInviteeRole() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITEE_ROLE", "Invitee must be an organizer");
    }

    public static ApiException alreadyMember() {
        return new ApiException(HttpStatus.CONFLICT, "ALREADY_MEMBER", "Account is already an active member");
    }

    public static ApiException invitationExists() {
        return new ApiException(HttpStatus.CONFLICT, "INVITATION_EXISTS", "A pending or accepted invitation already exists for this account");
    }

    public static ApiException invitationNotPending() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Invitation is not pending");
    }

    public static ApiException invitationNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "INVITATION_NOT_FOUND", "Invitation not found");
    }

    public static ApiException memberNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Organization member not found");
    }

    public static ApiException cannotRemoveOwner() {
        return new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_REMOVE_OWNER", "Cannot remove the organization owner");
    }

    public static ApiException cannotLeaveAsOwner() {
        return new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_LEAVE_AS_OWNER", "Organization owner cannot leave the organization");
    }

    public static ApiException invalidStatusTransition(String reason) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", reason);
    }

    public static ApiException notOrganizationMember() {
        return new ApiException(HttpStatus.BAD_REQUEST, "NOT_ORGANIZATION_MEMBER", "Account is not an active member of this organization");
    }
}
