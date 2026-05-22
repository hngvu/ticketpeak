package io.qzz.hoangvu.ticketpeak.api.offer.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.*;
import io.qzz.hoangvu.ticketpeak.api.offer.model.*;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import org.springframework.http.HttpStatus;
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
    private final EventService eventService;

    private static final Set<String> ISO_4217_CURRENCIES = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toUnmodifiableSet());

    public OfferService(OfferRepository offerRepository, EventService eventService) {
        this.offerRepository = offerRepository;
        this.eventService = eventService;
    }

    @Transactional
    @PreAuthorize("(hasRole('ORGANIZER') or hasRole('ADMIN')) and @orgSecurity.isEventOwnerOrMember(#eventId)")
    public OfferResponse createOffer(UUID eventId, CreateOfferRequest request) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_STATE",
                    "Cannot create offer for canceled or completed event");
        }

        String ticketTypeId = normalizeTicketTypeId(request.ticketTypeId());
        if (offerRepository.existsByEventIdAndTicketTypeId(eventId, ticketTypeId)) {
            throw new ApiException(HttpStatus.CONFLICT, "OFFER_TICKET_TYPE_ALREADY_EXISTS",
                    "Ticket type id is already registered for this event");
        }

        validateCurrency(request.currency());
        validateSaleWindows(request.saleWindows(), event);
        validateQuantity(request.eventTicketMinimum(), request.sellableQuantities());
        validateSeatingMode(request.seatingMode(), request.sectionId(), request.priceLevelId());

        Offer offer = Offer.builder()
                .eventId(eventId)
                .ticketTypeId(ticketTypeId)
                .name(trimToNull(request.name()))
                .description(trimToNull(request.description()))
                .currency(request.currency().trim().toUpperCase(Locale.ROOT))
                .faceValue(request.faceValue())
                .restrictedPayment(request.restrictedPayment())
                .eventTicketMinimum(request.eventTicketMinimum())
                .quantityAvailable(0)
                .quantitySold(0)
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
    @PreAuthorize("(hasRole('ORGANIZER') or hasRole('ADMIN')) and @orgSecurity.isEventOwnerOrMember(#eventId)")
    public OfferResponse updateOffer(UUID eventId, String ticketTypeId, UpdateOfferRequest request) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_STATE",
                    "Cannot update offer for canceled or completed event");
        }

        Offer offer = offerRepository.findByEventIdAndTicketTypeId(eventId, normalizeTicketTypeId(ticketTypeId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OFFER_NOT_FOUND",
                        "Offer does not exist for this event"));

        if (request.quantityAvailable() < offer.getQuantitySold()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_QUANTITY",
                    "Quantity available must be greater than or equal to quantity sold");
        }

        validateCurrency(request.currency());
        validateSaleWindowsForUpdate(request.saleWindows(), event);
        validateQuantity(request.eventTicketMinimum(), request.sellableQuantities());
        validateSeatingMode(request.seatingMode(), request.sectionId(), request.priceLevelId());

        offer.setName(trimToNull(request.name()));
        offer.setDescription(trimToNull(request.description()));
        offer.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        offer.setFaceValue(request.faceValue());
        offer.setRestrictedPayment(request.restrictedPayment());
        offer.setEventTicketMinimum(request.eventTicketMinimum());
        offer.setQuantityAvailable(request.quantityAvailable());
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
    @PreAuthorize("(hasRole('ORGANIZER') or hasRole('ADMIN')) and @orgSecurity.isEventOwnerOrMember(#eventId)")
    public void deleteOffer(UUID eventId, String ticketTypeId) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EVENT_STATE",
                    "Cannot delete offer for canceled or completed event");
        }

        offerRepository.deleteByEventIdAndTicketTypeId(eventId, normalizeTicketTypeId(ticketTypeId));
    }

    @PreAuthorize("(hasRole('ORGANIZER') or hasRole('ADMIN')) and @orgSecurity.isEventOwnerOrMember(#eventId)")
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
            throw new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
        }
        return offerRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(OfferResponse::from)
                .toList();
    }

    public OfferResponse getEventOffer(UUID eventId, String ticketTypeId) {
        EventResponse event = eventService.getEvent(eventId);
        if (event.status() == EventStatus.DRAFT) {
            throw new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
        }
        Offer offer = offerRepository.findByEventIdAndTicketTypeId(eventId, normalizeTicketTypeId(ticketTypeId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OFFER_NOT_FOUND",
                        "Offer does not exist for this event"));
        return OfferResponse.from(offer);
    }

    @PreAuthorize("(hasRole('ORGANIZER') or hasRole('ADMIN')) and @orgSecurity.isEventOwnerOrMember(#eventId)")
    public OfferResponse getEventOfferForPartner(UUID eventId, String ticketTypeId) {
        eventService.getEventForPartner(eventId);
        Offer offer = offerRepository.findByEventIdAndTicketTypeId(eventId, normalizeTicketTypeId(ticketTypeId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OFFER_NOT_FOUND",
                        "Offer does not exist for this event"));
        return OfferResponse.from(offer);
    }

    private void validateCurrency(String currency) {
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!ISO_4217_CURRENCIES.contains(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY",
                    "Currency must be a valid ISO 4217 code");
        }
    }

    private void validateSaleWindows(List<CreateSaleWindowRequest> windows, EventResponse event) {
        if (windows == null || windows.isEmpty()) {
            return;
        }

        for (CreateSaleWindowRequest window : windows) {
            if (window.endAt().isBefore(window.startAt())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                        window.type() + " end must not be before start");
            }

            if (event.saleStartAt() != null && window.startAt().isBefore(event.saleStartAt())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                        "Offer sale start must not be before event sale start");
            }
            if (event.saleEndAt() != null && window.endAt().isAfter(event.saleEndAt())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                        "Offer sale end must not be after event sale end");
            }
        }

        CreateSaleWindowRequest generalSale = windows.stream()
                .filter(w -> isGeneralSale(w.type()))
                .findFirst()
                .orElse(null);

        if (generalSale != null) {
            for (CreateSaleWindowRequest window : windows) {
                if (isPresale(window.type())) {
                    if (window.endAt().isAfter(generalSale.startAt())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                                "Presale must end before general sale starts");
                    }
                }
            }
        }
    }

    private void validateSaleWindowsForUpdate(List<UpdateSaleWindowRequest> windows, EventResponse event) {
        if (windows == null || windows.isEmpty()) {
            return;
        }

        for (UpdateSaleWindowRequest window : windows) {
            if (window.endAt().isBefore(window.startAt())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                        window.type() + " end must not be before start");
            }

            if (event.saleStartAt() != null && window.startAt().isBefore(event.saleStartAt())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                        "Offer sale start must not be before event sale start");
            }
            if (event.saleEndAt() != null && window.endAt().isAfter(event.saleEndAt())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                        "Offer sale end must not be after event sale end");
            }
        }

        UpdateSaleWindowRequest generalSale = windows.stream()
                .filter(w -> isGeneralSale(w.type()))
                .findFirst()
                .orElse(null);

        if (generalSale != null) {
            for (UpdateSaleWindowRequest window : windows) {
                if (isPresale(window.type())) {
                    if (window.endAt().isAfter(generalSale.startAt())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                                "Presale must end before general sale starts");
                    }
                }
            }
        }
    }

    private boolean isGeneralSale(SaleWindowType type) {
        return type == SaleWindowType.GENERAL_SALE;
    }

    private boolean isPresale(SaleWindowType type) {
        return type == SaleWindowType.PRESALE;
    }

    private void validateQuantity(Integer minimum, List<Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_QUANTITY",
                    "Sellable quantities must not be empty");
        }

        for (Integer quantity : quantities) {
            if (quantity < minimum) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_QUANTITY",
                        "Sellable quantities must be greater than or equal to the minimum");
            }
        }
    }

    private void validateSeatingMode(SeatingMode seatingMode, String sectionId, String priceLevelId) {
        if (seatingMode == SeatingMode.RESERVED_SEATING) {
            if (isBlank(sectionId) || isBlank(priceLevelId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_SEATING",
                        "Reserved seating offers require sectionId and priceLevelId");
            }
        } else if (seatingMode == SeatingMode.GENERAL_ADMISSION) {
            if (!isBlank(sectionId) || !isBlank(priceLevelId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_SEATING",
                        "General admission offers must not specify sectionId or priceLevelId");
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

    private String normalizeTicketTypeId(String ticketTypeId) {
        String normalized = trimToNull(ticketTypeId);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
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
