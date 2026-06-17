package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTicketTypeRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,

        @NotBlank(message = "must not be blank")
        @Size(max = 20, message = "must be at most 20 characters")
        @Pattern(regexp = "^[A-Z_]+$", message = "must contain only uppercase letters and underscores")
        String code
) {
}
