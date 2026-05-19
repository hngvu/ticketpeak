package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Section;

public record SectionResponse(String id, String manifestId, String description) {
    public static SectionResponse from(Section s) {
        return new SectionResponse(s.getId(), s.getManifest().getId(), s.getDescription());
    }
}
