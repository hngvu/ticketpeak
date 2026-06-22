package io.qzz.hoangvu.ticketpeak.api.venue.dto;

import java.util.List;

public record ManifestFullDetailResponse(
        ManifestResponse manifest,
        List<LevelResponse> levels,
        List<SectionResponse> sections,
        List<PriceLevelResponse> priceLevels,
        List<SeatRowResponse> rows,
        List<SeatResponse> seats
) {}
