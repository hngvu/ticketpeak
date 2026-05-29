package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertLookupRequest(
        @NotBlank String id,
        @NotBlank String description,
        String color
) {
    public UpsertLookupRequest(String id, String description) {
        this(id, description, null);
    }
}
