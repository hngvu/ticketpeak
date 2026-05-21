package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateVenueRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        String description,
        String thumbnailUrl,
        List<String> images
) {}
