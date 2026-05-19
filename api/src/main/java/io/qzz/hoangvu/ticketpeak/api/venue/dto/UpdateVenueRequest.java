package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpdateVenueRequest(
        String name,
        String address,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        String description,
        String thumbnailUrl,
        List<String> images
) {}
