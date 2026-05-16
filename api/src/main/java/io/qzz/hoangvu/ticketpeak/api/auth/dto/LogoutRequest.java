package io.qzz.hoangvu.ticketpeak.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "must not be blank")
        String refreshToken
) {
}
