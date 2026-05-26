package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTicketTypeRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "must contain only lowercase letters, numbers, and hyphens")
        String slug,

        @Size(max = 4000, message = "must be at most 4000 characters")
        String description
) {
}
