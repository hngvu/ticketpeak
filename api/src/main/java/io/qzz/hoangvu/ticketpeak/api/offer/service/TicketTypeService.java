package io.qzz.hoangvu.ticketpeak.api.offer.service;

import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.event.exception.EventException;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.CreateTicketTypeRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.TicketTypeResponse;
import io.qzz.hoangvu.ticketpeak.api.offer.exception.OfferException;
import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.TicketTypeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final OfferRepository offerRepository;
    private final EventService eventService;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository, OfferRepository offerRepository, EventService eventService) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.offerRepository = offerRepository;
        this.eventService = eventService;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public TicketTypeResponse createTicketType(UUID eventId, CreateTicketTypeRequest request) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw OfferException.invalidEventState("Cannot create ticket type for canceled or completed event");
        }

        if (ticketTypeRepository.existsByEventIdAndCode(eventId, request.code())) {
            throw OfferException.ticketTypeAlreadyExists();
        }

        TicketType ticketType = TicketType.builder()
                .eventId(eventId)
                .name(request.name().trim())
                .code(request.code())
                .build();

        return TicketTypeResponse.from(ticketTypeRepository.save(ticketType));
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public List<TicketTypeResponse> listEventTicketTypes(UUID eventId) {
        eventService.getEventForPartner(eventId);
        return ticketTypeRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(TicketTypeResponse::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public TicketTypeResponse getEventTicketTypeForPartner(UUID eventId, String code) {
        eventService.getEventForPartner(eventId);
        TicketType ticketType = ticketTypeRepository.findByEventIdAndCode(eventId, code)
                .orElseThrow(OfferException::ticketTypeNotFound);
        return TicketTypeResponse.from(ticketType);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @orgSecurity.isEventOwnerOrMember(#eventId))")
    public void deleteTicketType(UUID eventId, String code) {
        EventResponse event = eventService.getEventForPartner(eventId);

        if (event.status() == EventStatus.CANCELED || event.status() == EventStatus.COMPLETED) {
            throw OfferException.invalidEventState("Cannot delete ticket type for canceled or completed event");
        }

        TicketType ticketType = ticketTypeRepository.findByEventIdAndCode(eventId, code)
                .orElseThrow(OfferException::ticketTypeNotFound);

        // Check if there are any offers referencing this ticket type
        if (offerRepository.existsByEventIdAndTicketTypeId(eventId, ticketType.getId())) {
            throw OfferException.ticketTypeInUse("Cannot delete ticket type because it is referenced by one or more offers");
        }

        ticketTypeRepository.delete(ticketType);
    }
}
