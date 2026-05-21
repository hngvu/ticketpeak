package io.qzz.hoangvu.ticketpeak.api.event.dto;

import io.qzz.hoangvu.ticketpeak.api.event.model.Attraction;
import io.qzz.hoangvu.ticketpeak.api.event.model.AttractionType;
import java.util.UUID;

public record AttractionResponse(
    UUID id,
    String name,
    String slug,
    AttractionType type,
    String imageUrl,
    String description
) {
    public static AttractionResponse from(Attraction a) {
        if (a == null) return null;
        return new AttractionResponse(
            a.getId(),
            a.getName(),
            a.getSlug(),
            a.getType(),
            a.getImageUrl(),
            a.getDescription()
        );
    }
}
