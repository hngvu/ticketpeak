package io.qzz.hoangvu.ticketpeak.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "must be a valid email address")
        @NotBlank(message = "must not be blank")
        String email,
        @NotBlank(message = "must not be blank")
        String password
) {
}
