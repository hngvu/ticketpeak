package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Section;
import io.qzz.hoangvu.ticketpeak.api.venue.model.SectionType;
import lombok.Builder;

import java.util.Map;

@Builder
public record SectionResponse(
        String id,
        SectionType type,
        String name,
        String color,
        String levelId,
        Integer capacity,
        Map<String, Object> uiData
) {
    public static SectionResponse from(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .type(section.getType())
                .name(section.getName())
                .color(section.getColor())
                .levelId(section.getLevelId())
                .capacity(section.getCapacity())
                .uiData(section.getUiData())
                .build();
    }
}
