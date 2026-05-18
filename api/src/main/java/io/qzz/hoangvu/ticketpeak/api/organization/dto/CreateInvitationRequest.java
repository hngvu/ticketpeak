package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateInvitationRequest(
        @NotNull UUID inviteeAccountId
) {
}
