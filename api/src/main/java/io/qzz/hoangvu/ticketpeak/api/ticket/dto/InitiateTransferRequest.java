package io.qzz.hoangvu.ticketpeak.api.ticket.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InitiateTransferRequest(
        @NotNull UUID recipientAccountId
) {}
