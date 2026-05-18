package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MemberRequest(
        @NotNull(message = "accountId must not be null")
        UUID accountId
) {
}
