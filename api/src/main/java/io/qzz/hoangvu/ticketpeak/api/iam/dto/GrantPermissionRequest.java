package io.qzz.hoangvu.ticketpeak.api.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantPermissionRequest(
        @NotNull(message = "must not be null")
        UUID accountId,
        @NotBlank(message = "must not be blank")
        String permissionCode
) {
}
