package io.qzz.hoangvu.ticketpeak.api.resale.service;

import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodStatus;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutMethodRepository;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.CreateListingRequest;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.ResaleListingResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.exception.ResaleException;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListing;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleListingRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResaleListingService {

    private final ResaleListingRepository listingRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final PayoutMethodRepository payoutMethodRepository;

    @Transactional
    public ResaleListingResponse createListing(UUID sellerId, CreateListingRequest req) {
        Ticket ticket = ticketRepository.findByIdAndAccountId(req.ticketId(), sellerId)
                .orElseThrow(() -> ResaleException.ticketNotIssued());

        if (ticket.getStatus() != TicketStatus.ISSUED) {
            throw ResaleException.ticketNotIssued();
        }

        Event event = eventRepository.findById(ticket.getEventId())
                .orElseThrow(() -> ResaleException.eventNotFound());

        if (!event.isResaleEnabled()) {
            throw ResaleException.resaleDisabled();
        }

        if (event.getResalePriceCapPercent() != null) {
            BigDecimal maxPrice = ticket.getFaceValue()
                    .multiply(event.getResalePriceCapPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (req.askingPrice().compareTo(maxPrice) > 0) {
                throw ResaleException.priceExceedsCap();
            }
        }

        if (listingRepository.findActiveByTicketId(ticket.getId(), List.of(ResaleListingStatus.ACTIVE, ResaleListingStatus.RESERVED)).isPresent()) {
            throw ResaleException.activeListingExists();
        }

        int currentActive = listingRepository.countBySellerIdAndEventIdAndStatusIn(
                sellerId, event.getId(), List.of(ResaleListingStatus.ACTIVE, ResaleListingStatus.RESERVED)
        );

        if (currentActive >= event.getMaxResaleListingsPerUser()) {
            throw ResaleException.listingLimitExceeded();
        }

        payoutMethodRepository.findByAccountIdAndIsPrimaryTrueAndStatus(sellerId, PayoutMethodStatus.ACTIVE)
                .orElseThrow(ResaleException::noPrimaryPayoutMethod);

        ResaleListing listing = ResaleListing.builder()
                .ticketId(ticket.getId())
                .sellerId(sellerId)
                .eventId(event.getId())
                .offerId(ticket.getOfferId())
                .ticketTypeId(ticket.getTicketTypeId())
                .ticketTypeName(ticket.getTicketTypeName())
                .seatId(ticket.getSeatId())
                .sectionId(ticket.getSectionId())
                .seatingMode(ticket.getSeatingMode())
                .faceValue(ticket.getFaceValue())
                .askingPrice(req.askingPrice())
                .currency(ticket.getCurrency())
                .status(ResaleListingStatus.ACTIVE)
                .build();

        return ResaleListingResponse.from(listingRepository.save(listing), true);
    }

    @Transactional
    public ResaleListingResponse cancelListing(UUID sellerId, UUID listingId) {
        ResaleListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> ResaleException.listingNotFound(listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw ResaleException.listingNotFound(listingId);
        }

        if (listing.getStatus() != ResaleListingStatus.ACTIVE) {
            throw ResaleException.listingNotActive();
        }

        listing.setStatus(ResaleListingStatus.CANCELLED);
        return ResaleListingResponse.from(listingRepository.save(listing), true);
    }

    @Transactional(readOnly = true)
    public Page<ResaleListingResponse> getMyListings(UUID sellerId, Pageable pageable) {
        return listingRepository.findBySellerId(sellerId, pageable)
                .map(l -> ResaleListingResponse.from(l, true));
    }

    @Transactional(readOnly = true)
    public Page<ResaleListingResponse> browseListings(UUID eventId, Pageable pageable) {
        return listingRepository.findByEventIdAndStatus(eventId, ResaleListingStatus.ACTIVE, pageable)
                .map(l -> ResaleListingResponse.from(l, false));
    }
}
