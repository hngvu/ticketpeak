package io.qzz.hoangvu.ticketpeak.api.offer.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.ChargeRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.CreateOfferRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.OfferResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.UpdateOfferRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OfferService {

    private final OfferRepository offerRepository;
    private final EventService eventService;

    private static final Set<String> ISO_4217_CURRENCIES = Set.of(
            "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN",
            "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BOV",
            "BRL", "BSD", "BTN", "BWP", "BYN", "BZD", "CAD", "CDF", "CHE", "CHF",
            "CHW", "CLF", "CLP", "CNY", "COP", "COU", "CRC", "CUC", "CUP", "CVE",
            "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD",
            "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD",
            "HNL", "HTG", "HUF", "IDR", "ILS", "INR", "IQD", "IRR", "ISK", "JMD",
            "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD",
            "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL", "MGA",
            "MKD", "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN", "MXV",
            "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB",
            "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB",
            "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLE", "SLL",
            "SOS", "SRD", "SSP", "STN", "SVC", "SYP", "SZL", "THB", "TJS", "TMT",
            "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "USN",
            "UYI", "UYU", "UYW", "UZS", "VED", "VES", "VND", "VUV", "WST", "XAF",
            "XAG", "XAU", "XBA", "XBB", "XBC", "XBD", "XCD", "XDR", "XOF", "XPD",
            "XPF", "XPT", "XSU", "XTS", "XUA", "XXX", "YER", "ZAR", "ZMW", "ZWG"
    );

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
        validateSaleWindows(request, event);
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
                .presaleStartAt(request.presaleStartAt())
                .presaleEndAt(request.presaleEndAt())
                .saleStartAt(request.saleStartAt())
                .saleEndAt(request.saleEndAt())
                .seatingMode(request.seatingMode())
                .sectionId(trimToNull(request.sectionId()))
                .priceLevelId(trimToNull(request.priceLevelId()))
                .charges(normalizeCharges(request.charges()))
                .build();

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

        validateCurrency(request.currency());
        validateSaleWindows(request, event);
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
        offer.setPresaleStartAt(request.presaleStartAt());
        offer.setPresaleEndAt(request.presaleEndAt());
        offer.setSaleStartAt(request.saleStartAt());
        offer.setSaleEndAt(request.saleEndAt());
        offer.setSeatingMode(request.seatingMode());
        offer.setSectionId(trimToNull(request.sectionId()));
        offer.setPriceLevelId(trimToNull(request.priceLevelId()));
        offer.setCharges(normalizeCharges(request.charges()));

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

    public List<OfferResponse> listEventOffers(UUID eventId) {
        eventService.getEvent(eventId);
        return offerRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(OfferResponse::from)
                .toList();
    }

    public List<OfferResponse> listPublishedEventOffers(UUID eventId) {
        eventService.getEvent(eventId);
        return offerRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(OfferResponse::from)
                .toList();
    }

    public OfferResponse getEventOffer(UUID eventId, String ticketTypeId) {
        eventService.getEvent(eventId);
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

    private void validateSaleWindows(CreateOfferRequest request, EventResponse event) {
        validateWindowPair("presale", request.presaleStartAt(), request.presaleEndAt());
        validateWindowPair("sale", request.saleStartAt(), request.saleEndAt());

        Instant presaleStart = request.presaleStartAt();
        Instant presaleEnd = request.presaleEndAt();
        Instant saleStart = request.saleStartAt();
        Instant saleEnd = request.saleEndAt();

        if (presaleStart != null && saleStart != null && saleStart.isBefore(presaleStart)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "General sale must not start before presale starts");
        }
        if (presaleEnd != null && saleStart != null && presaleEnd.isAfter(saleStart)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "Presale must end before general sale starts");
        }
        if (presaleEnd != null && saleEnd != null && saleEnd.isBefore(presaleEnd)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "General sale must not end before presale ends");
        }

        if (event.saleStartAt() != null && saleStart != null && saleStart.isBefore(event.saleStartAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "Offer sale start must not be before event sale start");
        }
        if (event.saleEndAt() != null && saleEnd != null && saleEnd.isAfter(event.saleEndAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "Offer sale end must not be after event sale end");
        }
    }

    private void validateSaleWindows(UpdateOfferRequest request, EventResponse event) {
        validateWindowPair("presale", request.presaleStartAt(), request.presaleEndAt());
        validateWindowPair("sale", request.saleStartAt(), request.saleEndAt());

        Instant presaleStart = request.presaleStartAt();
        Instant presaleEnd = request.presaleEndAt();
        Instant saleStart = request.saleStartAt();
        Instant saleEnd = request.saleEndAt();

        if (presaleStart != null && saleStart != null && saleStart.isBefore(presaleStart)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "General sale must not start before presale starts");
        }
        if (presaleEnd != null && saleStart != null && presaleEnd.isAfter(saleStart)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "Presale must end before general sale starts");
        }
        if (presaleEnd != null && saleEnd != null && saleEnd.isBefore(presaleEnd)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "General sale must not end before presale ends");
        }

        if (event.saleStartAt() != null && saleStart != null && saleStart.isBefore(event.saleStartAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "Offer sale start must not be before event sale start");
        }
        if (event.saleEndAt() != null && saleEnd != null && saleEnd.isAfter(event.saleEndAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    "Offer sale end must not be after event sale end");
        }
    }

    private void validateWindowPair(String name, Instant start, Instant end) {
        if ((start == null) != (end == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    name + " start and end must both be provided");
        }
        if (start != null && end != null && end.isBefore(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_WINDOW",
                    name + " end must not be before start");
        }
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
