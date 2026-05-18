package io.qzz.hoangvu.ticketpeak.api.iam.dto;

import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePermissionRequest(
        @NotBlank(message = "must not be blank")
        String code,
        @NotBlank(message = "must not be blank")
        String name,
        @NotNull(message = "must not be null")
        PermissionScope scope,
        String action,
        String resource
) {
}
