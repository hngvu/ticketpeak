package io.qzz.hoangvu.ticketpeak.api.ticket.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public final class TicketException {
    private TicketException() {}

    public static ApiException notFound() {
        return new ApiException(NOT_FOUND, "TICKET_NOT_FOUND", "Ticket not found");
    }
    public static ApiException voided() {
        return new ApiException(GONE, "TICKET_VOIDED", "This ticket has been voided");
    }
    public static ApiException alreadyUsed() {
        return new ApiException(CONFLICT, "TICKET_ALREADY_CHECKED_IN", "Ticket has already been used");
    }
    public static ApiException notTransferable() {
        return new ApiException(CONFLICT, "TICKET_NOT_TRANSFERABLE",
                "Ticket must be in ISSUED status to transfer");
    }
    public static ApiException cannotTransferToSelf() {
        return new ApiException(BAD_REQUEST, "TICKET_TRANSFER_TO_SELF",
                "Cannot transfer a ticket to yourself");
    }
    public static ApiException transferNotFound() {
        return new ApiException(NOT_FOUND, "TICKET_TRANSFER_NOT_FOUND", "Transfer not found");
    }
    public static ApiException transferNotPending() {
        return new ApiException(CONFLICT, "TICKET_TRANSFER_NOT_PENDING",
                "Transfer is no longer pending");
    }
    public static ApiException transferExpired() {
        return new ApiException(GONE, "TICKET_TRANSFER_EXPIRED", "Transfer offer has expired");
    }
    public static ApiException invalidOtp() {
        return new ApiException(UNAUTHORIZED, "TICKET_INVALID_OTP",
                "QR code is invalid or expired");
    }
    public static ApiException invalidQrFormat() {
        return new ApiException(BAD_REQUEST, "TICKET_INVALID_QR_FORMAT", "Malformed QR payload");
    }
    public static ApiException wrongEvent() {
        return new ApiException(CONFLICT, "TICKET_WRONG_EVENT",
                "Ticket does not belong to this event");
    }
    public static ApiException pendingTransfer() {
        return new ApiException(CONFLICT, "TICKET_PENDING_TRANSFER",
                "Ticket has a pending transfer; check-in rejected");
    }
    public static ApiException transferNotEnabled() {
        return new ApiException(CONFLICT, "TICKET_TRANSFER_NOT_ENABLED",
                "Ticket transfer is not enabled for this event");
    }
    public static ApiException issuanceFailed(String detail) {
        return new ApiException(INTERNAL_SERVER_ERROR, "TICKET_ISSUANCE_FAILED", detail);
    }

    public static ApiException transferLimitReached() {
        return new ApiException(CONFLICT, "TICKET_TRANSFER_LIMIT_REACHED",
                "This ticket has reached the maximum number of transfers");
    }

    public static ApiException transferAlreadyPending() {
        return new ApiException(CONFLICT, "TICKET_TRANSFER_ALREADY_PENDING",
                "A transfer is already pending for this ticket");
    }
}
