package io.qzz.hoangvu.ticketpeak.api.event.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.exception.EventException;
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

import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;

    public EventService(
            EventRepository eventRepository,
            AttractionRepository attractionRepository,
            ClassificationRepository classificationRepository,
            EventClassificationRepository eventClassificationRepository,
            EventAttractionRepository eventAttractionRepository,
            EventManifestRepository eventManifestRepository,
            VenueService venueService,
            VenueRepository venueRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.attractionRepository = attractionRepository;
        this.classificationRepository = classificationRepository;
        this.eventClassificationRepository = eventClassificationRepository;
        this.eventAttractionRepository = eventAttractionRepository;
        this.eventManifestRepository = eventManifestRepository;
        this.venueService = venueService;
        this.venueRepository = venueRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isOwner(#req.organizationId()))")
    public EventResponse createEvent(CreateEventRequest req) {
        validateEventDates(req.startAt(), req.endAt(), req.saleStartAt(), req.saleEndAt());
        try {
            venueService.getVenue(req.venueId());
        } catch (ApiException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatus())) {
                throw EventException.venueNotFound();
            }
            throw ex;
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
                .maxTransferCount(req.maxTransferCount())
                .serviceFeePercent(req.serviceFeePercent() != null ? req.serviceFeePercent() : BigDecimal.ZERO)
                .resaleEnabled(false)
                .maxResaleListingsPerUser(1)
                .resalePriceCapPercent(null)
                .build();

        Event savedEvent = eventRepository.save(event);
        if (req.attractionIds() != null && !req.attractionIds().isEmpty()) {
            List<UUID> uniqueAttrIds = req.attractionIds().stream().distinct().toList();
            Set<UUID> existing = new HashSet<>(attractionRepository.findAllById(uniqueAttrIds)
                    .stream().map(Attraction::getId).toList());
            uniqueAttrIds.stream()
                    .filter(id -> !existing.contains(id))
                    .findFirst()
                    .ifPresent(id -> {
                        throw EventException.attractionNotFound(id);
                    });

            List<EventAttraction> attractionsToSave = uniqueAttrIds.stream()
                    .map(attrId -> new EventAttraction(savedEvent.getId(), attrId))
                    .toList();
            eventAttractionRepository.saveAll(attractionsToSave);
        }

        if (req.classificationIds() != null && !req.classificationIds().isEmpty()) {
            List<UUID> uniqueClassIds = req.classificationIds().stream().distinct().toList();
            Set<UUID> existing = new HashSet<>(classificationRepository.findAllById(uniqueClassIds)
                    .stream().map(Classification::getId).toList());
            uniqueClassIds.stream()
                    .filter(id -> !existing.contains(id))
                    .findFirst()
                    .ifPresent(id -> {
                        throw EventException.classificationNotFound(id);
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
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELED) {
            throw EventException.cannotUpdateEnded();
        }

        validateEventDates(req.startAt(), req.endAt(), req.saleStartAt(), req.saleEndAt());

        if (event.getStatus() != EventStatus.DRAFT && !event.getVenueId().equals(req.venueId())) {
            throw EventException.cannotChangeVenue();
        }

        try {
            venueService.getVenue(req.venueId());
        } catch (ApiException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatus())) {
                throw EventException.venueNotFound();
            }
            throw ex;
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
        event.setMaxTransferCount(req.maxTransferCount());
        event.setServiceFeePercent(req.serviceFeePercent() != null ? req.serviceFeePercent() : BigDecimal.ZERO);

        Event savedEvent = eventRepository.save(event);

        eventAttractionRepository.deleteByEventId(id);
        if (req.attractionIds() != null && !req.attractionIds().isEmpty()) {
            List<UUID> uniqueAttrIds = req.attractionIds().stream().distinct().toList();
            Set<UUID> existing = new HashSet<>(attractionRepository.findAllById(uniqueAttrIds)
                    .stream().map(Attraction::getId).toList());
            uniqueAttrIds.stream()
                    .filter(attrId -> !existing.contains(attrId))
                    .findFirst()
                    .ifPresent(attrId -> {
                        throw EventException.attractionNotFound(attrId);
                    });

            List<EventAttraction> attractionsToSave = uniqueAttrIds.stream()
                    .map(attrId -> new EventAttraction(id, attrId))
                    .toList();
            eventAttractionRepository.saveAll(attractionsToSave);
        }

        eventClassificationRepository.deleteByEventId(id);
        if (req.classificationIds() != null && !req.classificationIds().isEmpty()) {
            List<UUID> uniqueClassIds = req.classificationIds().stream().distinct().toList();
            Set<UUID> existing = new HashSet<>(classificationRepository.findAllById(uniqueClassIds)
                    .stream().map(Classification::getId).toList());
            uniqueClassIds.stream()
                    .filter(classId -> !existing.contains(classId))
                    .findFirst()
                    .ifPresent(classId -> {
                        throw EventException.classificationNotFound(classId);
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
                .orElseThrow(() -> EventException.notFound());
        if (event.getStatus() == EventStatus.DRAFT) {
            throw EventException.notFound();
        }
        return convertToResponse(event);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse getEventForPartner(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> EventException.notFound());
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
            List<UUID> classificationIds = classificationRepository.findDescendantIds(classificationId);
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

        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<EventResponse> content = convertToResponses(eventPage.getContent());
        return new org.springframework.data.domain.PageImpl<>(content, pageable, eventPage.getTotalElements());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse publishEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() != EventStatus.DRAFT) {
            throw EventException.notPublishable();
        }

        if (event.getVenueId() == null) {
            throw EventException.missingVenue();
        }

        var manifests = venueService.listManifests(event.getVenueId());
        var activeManifest = manifests.stream()
                .filter(m -> m.status() == io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus.PUBLISHED)
                .findFirst()
                .orElseThrow(() -> EventException.noPublishedManifest());

        String snapshotManifestId = getSnapshotManifestId(event.getId());
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
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() != EventStatus.PUBLISHED && event.getStatus() != EventStatus.ONSALE && event.getStatus() != EventStatus.POSTPONED) {
            throw EventException.cannotPostpone();
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
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() == EventStatus.CANCELED || event.getStatus() == EventStatus.COMPLETED) {
            throw EventException.cannotCancel();
        }

        event.setStatus(EventStatus.CANCELED);
        Event savedEvent = eventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse rescheduleEvent(UUID id, RescheduleEventRequest req) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() != EventStatus.POSTPONED && event.getStatus() != EventStatus.RESCHEDULED) {
            throw EventException.cannotReschedule();
        }

        Instant startAt = req.newStartAt();
        Instant endAt = req.newEndAt() != null ? req.newEndAt() : event.getEndAt();
        Instant saleStartAt = req.newSaleStartAt() != null ? req.newSaleStartAt() : event.getSaleStartAt();
        Instant saleEndAt = req.newSaleEndAt() != null ? req.newSaleEndAt() : event.getSaleEndAt();

        validateEventDates(startAt, endAt, saleStartAt, saleEndAt);

        event.setStatus(EventStatus.RESCHEDULED);
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
    public EventResponse resumeEvent(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() != EventStatus.POSTPONED && event.getStatus() != EventStatus.RESCHEDULED) {
            throw EventException.cannotResume();
        }

        validateEventDates(event.getStartAt(), event.getEndAt(), event.getSaleStartAt(), event.getSaleEndAt());

        eventPublisher.publishEvent(new EventStatusTransitionEvent(this, event.getId(), EventStatus.ONSALE));

        event.setStatus(EventStatus.ONSALE);
        Event savedEvent = eventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse startEventSales(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> EventException.notFound());

        if (event.getStatus() != EventStatus.PUBLISHED && event.getStatus() != EventStatus.OFFSALE) {
            throw EventException.cannotStartSales();
        }

        eventPublisher.publishEvent(new EventStatusTransitionEvent(this, event.getId(), EventStatus.ONSALE));

        event.setStatus(EventStatus.ONSALE);
        Event savedEvent = eventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#id))")
    public EventResponse cloneEvent(UUID id, CloneEventRequest req) {
        Event sourceEvent = eventRepository.findById(id)
                .orElseThrow(() -> EventException.notFound());

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

        // Clone kế thừa serviceFeePercent từ source event
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
                .maxTransferCount(sourceEvent.getMaxTransferCount())
                .serviceFeePercent(sourceEvent.getServiceFeePercent())
                .resaleEnabled(sourceEvent.isResaleEnabled())
                .maxResaleListingsPerUser(sourceEvent.getMaxResaleListingsPerUser())
                .resalePriceCapPercent(sourceEvent.getResalePriceCapPercent())
                .build();

        Event savedClone = eventRepository.save(clone);

        List<EventAttraction> attractionsToSave = eventAttractionRepository.findByEventId(id).stream()
                .map(ea -> new EventAttraction(savedClone.getId(), ea.getAttractionId()))
                .toList();
        if (!attractionsToSave.isEmpty()) {
            eventAttractionRepository.saveAll(attractionsToSave);
        }

        List<EventClassification> classificationsToSave = eventClassificationRepository.findByEventId(id).stream()
                .map(ec -> new EventClassification(savedClone.getId(), ec.getClassificationId()))
                .toList();
        if (!classificationsToSave.isEmpty()) {
            eventClassificationRepository.saveAll(classificationsToSave);
        }

        return convertToResponse(savedClone);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public EventResponse updateResaleConfig(UUID eventId, io.qzz.hoangvu.ticketpeak.api.resale.dto.UpdateResaleConfigRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> EventException.notFound());
        
        event.setResaleEnabled(req.resaleEnabled());
        event.setResalePriceCapPercent(req.resalePriceCapPercent());
        event.setMaxResaleListingsPerUser(req.maxResaleListingsPerUser());
        
        Event savedEvent = eventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    private List<EventResponse> convertToResponses(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        List<UUID> eventIds = events.stream().map(Event::getId).toList();

        List<EventAttraction> allEventAttractions = eventAttractionRepository.findByEventIdIn(eventIds);
        List<UUID> allAttractionIds = allEventAttractions.stream()
                .map(EventAttraction::getAttractionId)
                .distinct()
                .toList();

        Map<UUID, AttractionResponse> attractionResponseMap = new HashMap<>();
        if (!allAttractionIds.isEmpty()) {
            attractionRepository.findAllById(allAttractionIds).forEach(attraction -> {
                attractionResponseMap.put(attraction.getId(), AttractionResponse.from(attraction));
            });
        }

        Map<UUID, List<AttractionResponse>> attractionsByEvent = new HashMap<>();
        for (EventAttraction ea : allEventAttractions) {
            AttractionResponse ar = attractionResponseMap.get(ea.getAttractionId());
            if (ar != null) {
                attractionsByEvent.computeIfAbsent(ea.getEventId(), k -> new ArrayList<>()).add(ar);
            }
        }

        List<EventClassification> allEventClassifications = eventClassificationRepository.findByEventIdIn(eventIds);
        List<UUID> allClassificationIds = allEventClassifications.stream()
                .map(EventClassification::getClassificationId)
                .distinct()
                .toList();

        Map<UUID, ClassificationResponse> classificationResponseMap = new HashMap<>();
        if (!allClassificationIds.isEmpty()) {
            classificationRepository.findAllById(allClassificationIds).forEach(classification -> {
                classificationResponseMap.put(classification.getId(), ClassificationResponse.from(classification));
            });
        }

        Map<UUID, List<ClassificationResponse>> classificationsByEvent = new HashMap<>();
        for (EventClassification ec : allEventClassifications) {
            ClassificationResponse cr = classificationResponseMap.get(ec.getClassificationId());
            if (cr != null) {
                classificationsByEvent.computeIfAbsent(ec.getEventId(), k -> new ArrayList<>()).add(cr);
            }
        }

        Map<UUID, String> manifestMap = new HashMap<>();
        eventManifestRepository.findAllById(eventIds).forEach(em -> {
            manifestMap.put(em.getEventId(), em.getManifestId());
        });

        return events.stream().map(e -> {
            List<AttractionResponse> attractions = attractionsByEvent.getOrDefault(e.getId(), List.of());
            List<ClassificationResponse> classifications = classificationsByEvent.getOrDefault(e.getId(), List.of());
            String manifestId = manifestMap.get(e.getId());
            return EventResponse.from(e, attractions, classifications, manifestId);
        }).toList();
    }

    private EventResponse convertToResponse(Event e) {
        List<EventAttraction> eventAttractions = eventAttractionRepository.findByEventId(e.getId());
        List<UUID> attractionIds = eventAttractions.stream().map(EventAttraction::getAttractionId).toList();
        List<AttractionResponse> attractions = List.of();
        if (!attractionIds.isEmpty()) {
            Map<UUID, Attraction> attractionMap = attractionRepository.findAllById(attractionIds).stream()
                    .collect(Collectors.toMap(Attraction::getId, Function.identity()));
            attractions = attractionIds.stream()
                    .map(attractionMap::get)
                    .filter(Objects::nonNull)
                    .map(AttractionResponse::from)
                    .toList();
        }

        List<EventClassification> eventClassifications = eventClassificationRepository.findByEventId(e.getId());
        List<UUID> classificationIds = eventClassifications.stream().map(EventClassification::getClassificationId).toList();
        List<ClassificationResponse> classifications = List.of();
        if (!classificationIds.isEmpty()) {
            Map<UUID, Classification> classificationMap = classificationRepository.findAllById(classificationIds).stream()
                    .collect(Collectors.toMap(Classification::getId, Function.identity()));
            classifications = classificationIds.stream()
                    .map(classificationMap::get)
                    .filter(Objects::nonNull)
                    .map(ClassificationResponse::from)
                    .toList();
        }

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
            throw EventException.invalidDates("Event start date is required");
        }
        if (endAt != null && !startAt.isBefore(endAt)) {
            throw EventException.invalidDates("Event start date must be before end date");
        }
        if (saleStartAt != null && saleEndAt != null) {
            if (!saleStartAt.isBefore(saleEndAt)) {
                throw EventException.invalidDates("Sale start date must be before sale end date");
            }
        }
        if (saleStartAt != null) {
            if (endAt != null && saleStartAt.isAfter(endAt)) {
                throw EventException.invalidDates("Sale start date must be before or equal to event end date");
            } else if (endAt == null && saleStartAt.isAfter(startAt)) {
                throw EventException.invalidDates("Sale start date must be before or equal to event start date");
            }
        }
        if (saleEndAt != null) {
            if (endAt != null && saleEndAt.isAfter(endAt)) {
                throw EventException.invalidDates("Sale end date must be before or equal to event end date");
            } else if (endAt == null && saleEndAt.isAfter(startAt)) {
                throw EventException.invalidDates("Sale end date must be before or equal to event start date");
            }
        }
    }

    public static String getSnapshotManifestId(UUID eventId) {
        return "evt-" + eventId + "-snap";
    }
}
