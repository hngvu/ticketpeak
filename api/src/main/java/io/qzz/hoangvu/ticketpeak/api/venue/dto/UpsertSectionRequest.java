package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.model.SectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Map;

@Builder
public record UpsertSectionRequest(
        @NotBlank String id,
        @NotNull SectionType type,
        String name,
        String color,
        String levelId,
        Integer capacity,
        Map<String, Object> uiData
) {}
