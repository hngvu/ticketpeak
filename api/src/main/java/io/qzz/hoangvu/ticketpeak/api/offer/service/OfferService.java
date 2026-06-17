package io.qzz.hoangvu.ticketpeak.api.offer.service;

import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.event.exception.EventException;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.*;
import io.qzz.hoangvu.ticketpeak.api.offer.exception.OfferException;
import io.qzz.hoangvu.ticketpeak.api.offer.model.*;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.TicketTypeRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.service.VenueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OfferService {

    private final OfferRepository offerRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventService eventService;
    private final VenueService venueService;

    private static final Set<String> ISO_4217_CURRENCIES = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toUnmodifiableSet());

    public OfferService(OfferRepository offerRepository, TicketTypeRepository ticketTypeRepository, EventService eventService, VenueService venueService) {
        this.offerRepository = offerRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventService = eventService;
        this.venueService = venueService;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public OfferResponse createOffer(UUID eventId, CreateOfferRequest request) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw OfferException.invalidEventState("Cannot create offer for canceled or completed event");
        }

        TicketType ticketType = ticketTypeRepository.findById(request.ticketTypeId())
                .filter(tt -> tt.getEventId().equals(eventId))
                .orElseThrow(OfferException::ticketTypeNotFound);

        validateCurrency(request.currency());
        validateFaceValueAndMinimum(request.faceValue(), request.eventTicketMinimum());
        validateSaleWindows(request.saleWindows(), event);
        validateQuantity(request.eventTicketMinimum(), request.sellableQuantities());
        validateSeatingMode(request.seatingMode(), request.sectionId(), request.priceLevelId(), event);

        if (offerRepository.existsByEventIdAndCode(eventId, request.code())) {
            throw OfferException.codeAlreadyExists();
        }

        Offer offer = Offer.builder()
                .eventId(eventId)
                .ticketTypeId(ticketType.getId())
                .code(request.code())
                .name(trimToNull(request.name()))
                .description(trimToNull(request.description()))
                .currency(request.currency().trim().toUpperCase(Locale.ROOT))
                .faceValue(request.faceValue())
                .restrictedPayment(request.restrictedPayment())
                .eventTicketMinimum(request.eventTicketMinimum())
                .capacityCap(request.capacityCap())
                .sellableQuantities(normalizeQuantities(request.sellableQuantities()))
                .seatingMode(request.seatingMode())
                .sectionId(trimToNull(request.sectionId()))
                .priceLevelId(trimToNull(request.priceLevelId()))
                .charges(normalizeCharges(request.charges()))
                .build();

        if (request.saleWindows() != null) {
            for (CreateSaleWindowRequest sw : request.saleWindows()) {
                OfferSaleWindow window = OfferSaleWindow.builder()
                        .offer(offer)
                        .type(sw.type())
                        .startAt(sw.startAt())
                        .endAt(sw.endAt())
                        .build();
                offer.getSaleWindows().add(window);
            }
        }

        return OfferResponse.from(offerRepository.save(offer));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public OfferResponse updateOffer(UUID eventId, UUID offerId, UpdateOfferRequest request) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw OfferException.invalidEventState("Cannot update offer for canceled or completed event");
        }

        Offer offer = offerRepository.findByEventIdAndId(eventId, offerId)
                .orElseThrow(OfferException::notFound);


        validateCurrency(request.currency());
        validateFaceValueAndMinimum(request.faceValue(), request.eventTicketMinimum());
        validateSaleWindowsForUpdate(request.saleWindows(), event);
        validateQuantity(request.eventTicketMinimum(), request.sellableQuantities());
        validateSeatingMode(request.seatingMode(), request.sectionId(), request.priceLevelId(), event);

        if (!offer.getCode().equals(request.code()) && offerRepository.existsByEventIdAndCode(eventId, request.code())) {
            throw OfferException.codeAlreadyExists();
        }

        offer.setCode(request.code());
        offer.setName(trimToNull(request.name()));
        offer.setDescription(trimToNull(request.description()));
        offer.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        offer.setFaceValue(request.faceValue());
        offer.setRestrictedPayment(request.restrictedPayment());
        offer.setEventTicketMinimum(request.eventTicketMinimum());
        offer.setCapacityCap(request.capacityCap());
        offer.setSellableQuantities(normalizeQuantities(request.sellableQuantities()));
        offer.setSeatingMode(request.seatingMode());
        offer.setSectionId(trimToNull(request.sectionId()));
        offer.setPriceLevelId(trimToNull(request.priceLevelId()));
        offer.setCharges(normalizeCharges(request.charges()));

        offer.getSaleWindows().clear();
        if (request.saleWindows() != null) {
            for (UpdateSaleWindowRequest sw : request.saleWindows()) {
                OfferSaleWindow window = OfferSaleWindow.builder()
                        .offer(offer)
                        .type(sw.type())
                        .startAt(sw.startAt())
                        .endAt(sw.endAt())
                        .build();
                offer.getSaleWindows().add(window);
            }
        }

        return OfferResponse.from(offerRepository.save(offer));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public void deleteOffer(UUID eventId, UUID offerId) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw OfferException.invalidEventState("Cannot delete offer for canceled or completed event");
        }

        Offer offer = offerRepository.findByEventIdAndId(eventId, offerId)
                .orElseThrow(OfferException::notFound);

        // TODO: Enforce the active-sales guard: block offer deletion once ticket sales or orders exist.
        // This will be programmatically checked once OrderRepository and TicketRepository are implemented.

        offerRepository.delete(offer);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public List<OfferResponse> listEventOffers(UUID eventId) {
        eventService.getEventForPartner(eventId);
        return offerRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(OfferResponse::from)
                .toList();
    }

    public List<OfferResponse> listPublishedEventOffers(UUID eventId) {
        EventResponse event = eventService.getEvent(eventId);
        if (event.status() == EventStatus.DRAFT) {
            throw EventException.notFound();
        }
        return offerRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(OfferResponse::from)
                .toList();
    }

    public OfferResponse getEventOffer(UUID eventId, UUID offerId) {
        EventResponse event = eventService.getEvent(eventId);
        if (event.status() == EventStatus.DRAFT) {
            throw EventException.notFound();
        }
        Offer offer = offerRepository.findByEventIdAndId(eventId, offerId)
                .orElseThrow(OfferException::notFound);
        return OfferResponse.from(offer);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public OfferResponse getEventOfferForPartner(UUID eventId, UUID offerId) {
        eventService.getEventForPartner(eventId);
        Offer offer = offerRepository.findByEventIdAndId(eventId, offerId)
                .orElseThrow(OfferException::notFound);
        return OfferResponse.from(offer);
    }

    private void validateCurrency(String currency) {
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!ISO_4217_CURRENCIES.contains(normalized)) {
            throw OfferException.invalidCurrency();
        }
    }

    private record SaleWindow(SaleWindowType type, Instant startAt, Instant endAt) {}

    private void validateFaceValueAndMinimum(BigDecimal faceValue, Integer eventTicketMinimum) {
        if (faceValue == null || faceValue.compareTo(BigDecimal.ZERO) < 0) {
            throw OfferException.invalidPrice();
        }
        if (eventTicketMinimum == null || eventTicketMinimum < 1) {
            throw OfferException.invalidLimits();
        }
    }

    private void validateSaleWindowsCommon(List<SaleWindow> windows, EventResponse event) {
        if (windows == null || windows.isEmpty()) {
            return;
        }

        for (SaleWindow window : windows) {
            if (window.endAt().isBefore(window.startAt())) {
                throw OfferException.invalidWindow(window.type() + " end must not be before start");
            }

            if (event.saleStartAt() != null && window.startAt().isBefore(event.saleStartAt())) {
                throw OfferException.invalidWindow("Offer sale start must not be before event sale start");
            }
            if (event.saleEndAt() != null && window.endAt().isAfter(event.saleEndAt())) {
                throw OfferException.invalidWindow("Offer sale end must not be after event sale end");
            }
        }

        SaleWindow generalSale = windows.stream()
                .filter(w -> isGeneralSale(w.type()))
                .findFirst()
                .orElse(null);

        if (generalSale != null) {
            for (SaleWindow window : windows) {
                if (isPresale(window.type())) {
                    if (window.endAt().isAfter(generalSale.startAt())) {
                        throw OfferException.invalidWindow("Presale must end before general sale starts");
                    }
                }
            }
        }
    }

    private void validateSaleWindows(List<CreateSaleWindowRequest> windows, EventResponse event) {
        if (windows == null || windows.isEmpty()) {
            return;
        }
        List<SaleWindow> mapped = windows.stream()
                .map(w -> new SaleWindow(w.type(), w.startAt(), w.endAt()))
                .toList();
        validateSaleWindowsCommon(mapped, event);
    }

    private void validateSaleWindowsForUpdate(List<UpdateSaleWindowRequest> windows, EventResponse event) {
        if (windows == null || windows.isEmpty()) {
            return;
        }
        List<SaleWindow> mapped = windows.stream()
                .map(w -> new SaleWindow(w.type(), w.startAt(), w.endAt()))
                .toList();
        validateSaleWindowsCommon(mapped, event);
    }

    private boolean isGeneralSale(SaleWindowType type) {
        return type == SaleWindowType.GENERAL_SALE;
    }

    private boolean isPresale(SaleWindowType type) {
        return type == SaleWindowType.PRESALE;
    }

    private void validateQuantity(Integer minimum, List<Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            throw OfferException.invalidQuantity("Sellable quantities must not be empty");
        }

        for (Integer quantity : quantities) {
            if (quantity < minimum) {
                throw OfferException.invalidQuantity("Sellable quantities must be greater than or equal to the minimum");
            }
        }
    }

    private void validateSeatingMode(SeatingMode seatingMode, String sectionId, String priceLevelId, EventResponse event) {
        if (seatingMode == SeatingMode.RESERVED_SEATING) {
            if (isBlank(sectionId) || isBlank(priceLevelId)) {
                throw OfferException.invalidSeating("Reserved seating offers require sectionId and priceLevelId");
            }

            // Resolve manifest ID
            String manifestId;
            if (event.status() == EventStatus.DRAFT) {
                var manifests = venueService.listManifests(event.venueId());
                var activeManifest = manifests.stream()
                        .filter(m -> m.status() == io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus.PUBLISHED)
                        .findFirst()
                        .orElseThrow(OfferException::noPublishedManifest);
                manifestId = activeManifest.id();
            } else {
                manifestId = event.manifestId() != null ? event.manifestId() : EventService.getSnapshotManifestId(event.id());
            }

            // Validate section and price level exist in manifest lookup tables
            var sections = venueService.listSections(manifestId);
            boolean sectionExists = sections.stream().anyMatch(s -> s.id().equals(sectionId));
            if (!sectionExists) {
                throw OfferException.sectionNotFound(sectionId, manifestId);
            }

            var priceLevels = venueService.listPriceLevels(manifestId);
            boolean priceLevelExists = priceLevels.stream().anyMatch(p -> p.id().equals(priceLevelId));
            if (!priceLevelExists) {
                throw OfferException.priceLevelNotFound(priceLevelId, manifestId);
            }

        } else if (seatingMode == SeatingMode.GENERAL_ADMISSION) {
            if (!isBlank(sectionId) || !isBlank(priceLevelId)) {
                if (isBlank(sectionId) || isBlank(priceLevelId)) {
                    throw OfferException.invalidSeating("General admission offers specifying seating must include both sectionId and priceLevelId");
                }

                // Resolve manifest ID
                String manifestId;
                if (event.status() == EventStatus.DRAFT) {
                    var manifests = venueService.listManifests(event.venueId());
                    var activeManifest = manifests.stream()
                            .filter(m -> m.status() == io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus.PUBLISHED)
                            .findFirst()
                            .orElseThrow(OfferException::noPublishedManifest);
                    manifestId = activeManifest.id();
                } else {
                    manifestId = event.manifestId() != null ? event.manifestId() : EventService.getSnapshotManifestId(event.id());
                }

                // Validate section and price level exist in manifest lookup tables
                    var sections = venueService.listSections(manifestId);
                    boolean sectionExists = sections.stream().anyMatch(s -> s.id().equals(sectionId));
                    if (!sectionExists) {
                        throw OfferException.sectionNotFound(sectionId, manifestId);
                    }

                    var priceLevels = venueService.listPriceLevels(manifestId);
                    boolean priceLevelExists = priceLevels.stream().anyMatch(p -> p.id().equals(priceLevelId));
                    if (!priceLevelExists) {
                        throw OfferException.priceLevelNotFound(priceLevelId, manifestId);
                    }
                }
            }
        }

    private List<Integer> normalizeQuantities(List<Integer> quantities) {
        return quantities.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private List<OfferCharge> normalizeCharges(List<ChargeRequest> charges) {
        if (charges == null || charges.isEmpty()) {
            return List.of();
        }

        return charges.stream()
                .map(charge -> new OfferCharge(
                        trimToNull(charge.name()),
                        charge.type(),
                        charge.amount(),
                        charge.isPercentage()
                ))
                .toList();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
