package io.qzz.hoangvu.ticketpeak.api.event.dto;

import io.qzz.hoangvu.ticketpeak.api.event.model.Classification;
import java.util.UUID;

public record ClassificationResponse(
    UUID id,
    String name,
    String slug,
    Integer level,
    Boolean isActive,
    UUID parentId
) {
    public static ClassificationResponse from(Classification c) {
        if (c == null) return null;
        return new ClassificationResponse(
            c.getId(),
            c.getName(),
            c.getSlug(),
            c.getLevel(),
            c.getIsActive(),
            c.getParentId()
        );
    }
}
