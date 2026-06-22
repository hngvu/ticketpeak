package io.qzz.hoangvu.ticketpeak.api.event.dto;

import io.qzz.hoangvu.ticketpeak.api.venue.dto.LevelResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.ManifestResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.SeatResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.SeatRowResponse;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.SectionResponse;

import java.util.List;

public record EventManifestDetailResponse(
        ManifestResponse manifest,
        List<LevelResponse> levels,
        List<SectionResponse> sections,
        List<EventPriceLevelResponse> priceLevels,
        List<SeatRowResponse> rows,
        List<SeatResponse> seats
) {}
