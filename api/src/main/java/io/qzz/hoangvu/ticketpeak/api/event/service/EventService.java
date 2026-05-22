package io.qzz.hoangvu.ticketpeak.api.event.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.dto.*;
import io.qzz.hoangvu.ticketpeak.api.event.model.*;
import io.qzz.hoangvu.ticketpeak.api.event.repository.*;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.service.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AttractionRepository attractionRepository;
    private final ClassificationRepository classificationRepository;
    private final EventClassificationRepository eventClassificationRepository;
    private final EventAttractionRepository eventAttractionRepository;
    private final EventManifestRepository eventManifestRepository;
    private final VenueService venueService;
    private final VenueRepository venueRepository;

    public EventService(
            EventRepository eventRepository,
            AttractionRepository attractionRepository,
            ClassificationRepository classificationRepository,
            EventClassificationRepository eventClassificationRepository,
            EventAttractionRepository eventAttractionRepository,
            EventManifestRepository eventManifestRepository,
            VenueService venueService,
            VenueRepository venueRepository
    ) {
        this.eventRepository = eventRepository;
        this.attractionRepository = attractionRepository;
        this.classificationRepository = classificationRepository;
        this.eventClassificationRepository = eventClassificationRepository;
        this.eventAttractionRepository = eventAttractionRepository;
        this.eventManifestRepository = eventManifestRepository;
        this.venueService = venueService;
        this.venueRepository = venueRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isOwner(#req.organizationId()))")
    public EventResponse createEvent(CreateEventRequest req) {
        validateEventDates(req.startAt(), req.endAt(), req.saleStartAt(), req.saleEndAt());
        try {
            venueService.getVenue(req.venueId());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VENUE_NOT_FOUND", "The assigned venue does not exist");
        }

        String slug = req.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(req.title());
        }
        slug = slug.toLowerCase();

        if (eventRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Event event = Event.builder()
                .organizationId(req.organizationId())
                .venueId(req.venueId())
                .title(req.title())
                .slug(slug)
                .description(req.description())
                .status(EventStatus.DRAFT)
                .startAt(req.startAt())
                .endAt(req.endAt())
                .timezone(req.timezone())
                .saleStartAt(req.saleStartAt())
                .saleEndAt(req.saleEndAt())
                .restrictSingleSeat(req.restrictSingleSeat())
                .safeTixEnabled(req.safeTixEnabled())
                .transferEnabled(req.transferEnabled())
                .build();

        Event savedEvent = eventRepository.save(event);
        if (req.attractionIds() != null && !req.attractionIds().isEmpty()) {
            List<UUID> uniqueAttrIds = req.attractionIds().stream().distinct().toList();
            java.util.Set<UUID> existing = new java.util.HashSet<>(attractionRepository.findAllById(uniqueAttrIds)
                    .stream().map(Attraction::getId).toList());
            uniqueAttrIds.stream()
                    .filter(id -> !existing.contains(id))
                    .findFirst()
                    .ifPresent(id -> {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "ATTRACTION_NOT_FOUND", "Attraction with id " + id + " does not exist");
                    });

            List<EventAttraction> attractionsToSave = uniqueAttrIds.stream()
                    .map(attrId -> new EventAttraction(savedEvent.getId(), attrId))
                    .toList();
            eventAttractionRepository.saveAll(attractionsToSave);
        }

        if (req.classificationIds() != null && !req.classificationIds().isEmpty()) {
            List<UUID> uniqueClassIds = req.classificationIds().stream().distinct().toList();
            java.util.Set<UUID> existing = new java.util.HashSet<>(classificationRepository.findAllById(uniqueClassIds)
                    .stream().map(Classification::getId).toList());
            uniqueClassIds.stream()
                    .filter(id -> !existing.contains(id))
                    .findFirst()
                    .ifPresent(id -> {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "CLASSIFICATION_NOT_FOUND", "Classification with id " + id + " does not exist");
                    });

            List<EventClassification> classificationsToSave = uniqueClassIds.stream()
                    .map(classId -> new EventClassification(savedEvent.getId(), classId))
                    .toList();
            eventClassificationRepository.saveAll(classificationsToSave);
        }

        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse updateEvent(UUID id, UpdateEventRequest req) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Cannot update ended or cancelled event");
        }

        validateEventDates(req.startAt(), req.endAt(), req.saleStartAt(), req.saleEndAt());

        try {
            venueService.getVenue(req.venueId());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VENUE_NOT_FOUND", "The assigned venue does not exist");
        }

        event.setVenueId(req.venueId());
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setStartAt(req.startAt());
        event.setEndAt(req.endAt());
        event.setTimezone(req.timezone());
        event.setSaleStartAt(req.saleStartAt());
        event.setSaleEndAt(req.saleEndAt());
        event.setRestrictSingleSeat(req.restrictSingleSeat());
        event.setSafeTixEnabled(req.safeTixEnabled());
        event.setTransferEnabled(req.transferEnabled());

        Event savedEvent = eventRepository.save(event);

        eventAttractionRepository.deleteByEventId(id);
        if (req.attractionIds() != null && !req.attractionIds().isEmpty()) {
            List<UUID> uniqueAttrIds = req.attractionIds().stream().distinct().toList();
            java.util.Set<UUID> existing = new java.util.HashSet<>(attractionRepository.findAllById(uniqueAttrIds)
                    .stream().map(Attraction::getId).toList());
            uniqueAttrIds.stream()
                    .filter(attrId -> !existing.contains(attrId))
                    .findFirst()
                    .ifPresent(attrId -> {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "ATTRACTION_NOT_FOUND", "Attraction with id " + attrId + " does not exist");
                    });

            List<EventAttraction> attractionsToSave = uniqueAttrIds.stream()
                    .map(attrId -> new EventAttraction(id, attrId))
                    .toList();
            eventAttractionRepository.saveAll(attractionsToSave);
        }

        eventClassificationRepository.deleteByEventId(id);
        if (req.classificationIds() != null && !req.classificationIds().isEmpty()) {
            List<UUID> uniqueClassIds = req.classificationIds().stream().distinct().toList();
            java.util.Set<UUID> existing = new java.util.HashSet<>(classificationRepository.findAllById(uniqueClassIds)
                    .stream().map(Classification::getId).toList());
            uniqueClassIds.stream()
                    .filter(classId -> !existing.contains(classId))
                    .findFirst()
                    .ifPresent(classId -> {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "CLASSIFICATION_NOT_FOUND", "Classification with id " + classId + " does not exist");
                    });

            List<EventClassification> classificationsToSave = uniqueClassIds.stream()
                    .map(classId -> new EventClassification(id, classId))
                    .toList();
            eventClassificationRepository.saveAll(classificationsToSave);
        }

        return convertToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));
        if (event.getStatus() == EventStatus.DRAFT) {
            throw new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
        }
        return convertToResponse(event);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse getEventForPartner(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));
        return convertToResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(
            String query,
            List<EventStatus> statuses,
            List<EventStatus> excludeStatuses,
            UUID venueId,
            UUID organizationId,
            String city,
            String country,
            UUID classificationId,
            UUID attractionId,
            Instant startAfter,
            Instant startBefore,
            Instant endAfter,
            Instant endBefore,
            Pageable pageable
    ) {
        List<UUID> venueIdsByLocation = null;
        if (city != null || country != null) {
            venueIdsByLocation = venueRepository.searchByLocation(city, country).stream()
                    .map(v -> v.getId())
                    .toList();
        }

        List<UUID> venueIdsByQuery = null;
        List<UUID> eventIdsForClassificationsByQuery = null;
        List<UUID> eventIdsForAttractionsByQuery = null;

        if (query != null && !query.isBlank()) {
            venueIdsByQuery = venueRepository.searchByKeyword(query).stream()
                    .map(v -> v.getId())
                    .toList();

            List<Attraction> attractions = attractionRepository.searchByNameOrDescription(query);
            if (!attractions.isEmpty()) {
                List<UUID> attractionIds = attractions.stream().map(Attraction::getId).toList();
                eventIdsForAttractionsByQuery = eventAttractionRepository.findEventIdsByAttractionIds(attractionIds);
            }

            List<Classification> classifications = classificationRepository.searchByName(query);
            if (!classifications.isEmpty()) {
                List<UUID> classificationIds = classifications.stream().map(Classification::getId).toList();
                eventIdsForClassificationsByQuery = eventClassificationRepository.findEventIdsByClassificationIds(classificationIds);
            }
        }

        List<UUID> explicitEventIdsForClassifications = null;
        if (classificationId != null) {
            List<UUID> classificationIds = new java.util.ArrayList<>();
            findDescendantClassifications(classificationId, classificationIds);
            explicitEventIdsForClassifications = eventClassificationRepository.findEventIdsByClassificationIds(classificationIds);
        }

        List<UUID> explicitEventIdsForAttractions = null;
        if (attractionId != null) {
            explicitEventIdsForAttractions = eventAttractionRepository.findEventIdsByAttractionIds(List.of(attractionId));
        }

        Specification<Event> spec = EventSpecification.filterEvents(
                query, statuses, excludeStatuses, venueId, organizationId,
                startAfter, startBefore, endAfter, endBefore,
                venueIdsByQuery, eventIdsForClassificationsByQuery, eventIdsForAttractionsByQuery,
                explicitEventIdsForClassifications, venueIdsByLocation, explicitEventIdsForAttractions
        );

        return eventRepository.findAll(spec, pageable).map(this::convertToResponse);
    }

    private void findDescendantClassifications(UUID parentId, List<UUID> accumulator) {
        accumulator.add(parentId);
        List<Classification> children = classificationRepository.findByParentId(parentId);
        for (Classification child : children) {
            findDescendantClassifications(child.getId(), accumulator);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse publishEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Only DRAFT events can be published");
        }

        if (event.getVenueId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_VENUE", "Event must have an assigned venue to be published");
        }

        var manifests = venueService.listManifests(event.getVenueId());
        var activeManifest = manifests.stream()
                .filter(m -> m.status() == io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus.PUBLISHED)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "NO_PUBLISHED_MANIFEST", "The assigned venue does not have a published manifest"));

        String snapshotManifestId = "evt-" + event.getId() + "-snap";
        var cloneRequest = new io.qzz.hoangvu.ticketpeak.api.venue.dto.CloneManifestRequest(
                snapshotManifestId,
                "Snapshot for event " + event.getTitle()
        );
        try {
            venueService.cloneManifest(activeManifest.id(), cloneRequest);
        } catch (ApiException ex) {
            if (!HttpStatus.CONFLICT.equals(ex.getStatus())) {
                throw ex;
            }
        }

        eventManifestRepository.save(new EventManifest(event.getId(), snapshotManifestId));

        event.setStatus(EventStatus.PUBLISHED);
        Event savedEvent = eventRepository.save(event);

        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse postponeEvent(UUID id, PostponeEventRequest req) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED && event.getStatus() != EventStatus.ONSALE && event.getStatus() != EventStatus.POSTPONED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Only published, on-sale, or postponed events can be postponed");
        }

        Instant startAt = req.newStartAt();
        Instant endAt = req.newEndAt() != null ? req.newEndAt() : event.getEndAt();
        Instant saleStartAt = req.newSaleStartAt() != null ? req.newSaleStartAt() : event.getSaleStartAt();
        Instant saleEndAt = req.newSaleEndAt() != null ? req.newSaleEndAt() : event.getSaleEndAt();

        validateEventDates(startAt, endAt, saleStartAt, saleEndAt);

        event.setStatus(EventStatus.POSTPONED);
        event.setStartAt(req.newStartAt());
        event.setEndAt(req.newEndAt());
        if (req.newSaleStartAt() != null) {
            event.setSaleStartAt(req.newSaleStartAt());
        }
        if (req.newSaleEndAt() != null) {
            event.setSaleEndAt(req.newSaleEndAt());
        }

        Event savedEvent = eventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse cancelEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (event.getStatus() == EventStatus.CANCELED || event.getStatus() == EventStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Event is already in ended or cancelled state");
        }

        event.setStatus(EventStatus.CANCELED);
        Event savedEvent = eventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse cloneEvent(UUID id, CloneEventRequest req) {
        Event sourceEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        Instant startAt = req.startAt() != null ? req.startAt() : sourceEvent.getStartAt();
        Instant endAt = req.endAt();
        Instant saleStartAt = req.saleStartAt();
        Instant saleEndAt = req.saleEndAt();

        if (req.startAt() != null && sourceEvent.getStartAt() != null) {
            java.time.Duration offset = java.time.Duration.between(sourceEvent.getStartAt(), req.startAt());
            if (endAt == null && sourceEvent.getEndAt() != null) {
                endAt = sourceEvent.getEndAt().plus(offset);
            }
            if (saleStartAt == null && sourceEvent.getSaleStartAt() != null) {
                saleStartAt = sourceEvent.getSaleStartAt().plus(offset);
            }
            if (saleEndAt == null && sourceEvent.getSaleEndAt() != null) {
                saleEndAt = sourceEvent.getSaleEndAt().plus(offset);
            }
        } else {
            if (endAt == null) endAt = sourceEvent.getEndAt();
            if (saleStartAt == null) saleStartAt = sourceEvent.getSaleStartAt();
            if (saleEndAt == null) saleEndAt = sourceEvent.getSaleEndAt();
        }

        validateEventDates(startAt, endAt, saleStartAt, saleEndAt);

        String slug = req.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(req.title());
        }
        slug = slug.toLowerCase();

        if (eventRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Event clone = Event.builder()
                .organizationId(sourceEvent.getOrganizationId())
                .venueId(sourceEvent.getVenueId())
                .title(req.title())
                .slug(slug)
                .description(sourceEvent.getDescription())
                .status(EventStatus.DRAFT)
                .startAt(startAt)
                .endAt(endAt)
                .timezone(sourceEvent.getTimezone())
                .saleStartAt(saleStartAt)
                .saleEndAt(saleEndAt)
                .restrictSingleSeat(sourceEvent.isRestrictSingleSeat())
                .safeTixEnabled(sourceEvent.isSafeTixEnabled())
                .transferEnabled(sourceEvent.isTransferEnabled())
                .build();

        Event savedClone = eventRepository.save(clone);

        eventAttractionRepository.findByEventId(id).forEach(ea ->
                eventAttractionRepository.save(new EventAttraction(savedClone.getId(), ea.getAttractionId()))
        );

        eventClassificationRepository.findByEventId(id).forEach(ec ->
                eventClassificationRepository.save(new EventClassification(savedClone.getId(), ec.getClassificationId()))
        );

        return convertToResponse(savedClone);
    }

    private EventResponse convertToResponse(Event e) {
        List<AttractionResponse> attractions = eventAttractionRepository.findByEventId(e.getId()).stream()
                .map(ea -> attractionRepository.findById(ea.getAttractionId()).orElse(null))
                .filter(Objects::nonNull)
                .map(AttractionResponse::from)
                .toList();

        List<ClassificationResponse> classifications = eventClassificationRepository.findByEventId(e.getId()).stream()
                .map(ec -> classificationRepository.findById(ec.getClassificationId()).orElse(null))
                .filter(Objects::nonNull)
                .map(ClassificationResponse::from)
                .toList();

        String manifestId = eventManifestRepository.findById(e.getId())
                .map(EventManifest::getManifestId)
                .orElse(null);

        return EventResponse.from(e, attractions, classifications, manifestId);
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }

    private void validateEventDates(Instant startAt, Instant endAt, Instant saleStartAt, Instant saleEndAt) {
        if (startAt == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Event start date is required");
        }
        if (endAt != null && !startAt.isBefore(endAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Event start date must be before end date");
        }
        if (saleStartAt != null && saleEndAt != null) {
            if (!saleStartAt.isBefore(saleEndAt)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Sale start date must be before sale end date");
            }
        }
        if (saleStartAt != null) {
            if (endAt != null && saleStartAt.isAfter(endAt)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Sale start date must be before or equal to event end date");
            } else if (endAt == null && saleStartAt.isAfter(startAt)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Sale start date must be before or equal to event start date");
            }
        }
        if (saleEndAt != null) {
            if (endAt != null && saleEndAt.isAfter(endAt)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Sale end date must be before or equal to event end date");
            } else if (endAt == null && saleEndAt.isAfter(startAt)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_DATES", "Sale end date must be before or equal to event start date");
            }
        }
    }
}
