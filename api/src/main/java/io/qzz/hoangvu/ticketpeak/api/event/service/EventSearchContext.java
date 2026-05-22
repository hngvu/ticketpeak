package io.qzz.hoangvu.ticketpeak.api.event.service;

import java.util.List;
import java.util.UUID;

record EventSearchContext(
        List<UUID> venueIdsByLocation,
        List<UUID> venueIdsByQuery,
        List<UUID> eventIdsForClassificationsByQuery,
        List<UUID> eventIdsForAttractionsByQuery,
        List<UUID> explicitEventIdsForClassifications,
        List<UUID> explicitEventIdsForAttractions
) {}
