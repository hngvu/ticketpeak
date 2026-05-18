package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank String token
) {
}
