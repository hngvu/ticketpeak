package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatusTransitionEvent;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryGaRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.GAAreaRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.SeatRepository;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class InventoryEventListener {

    private final InventoryGaRepository inventoryGaRepository;
    private final InventorySeatRepository inventorySeatRepository;
    private final EventManifestRepository eventManifestRepository;
    private final GAAreaRepository gaAreaRepository;
    private final SeatRepository seatRepository;
    private final OfferRepository offerRepository;

    public InventoryEventListener(
            InventoryGaRepository inventoryGaRepository,
            InventorySeatRepository inventorySeatRepository,
            EventManifestRepository eventManifestRepository,
            GAAreaRepository gaAreaRepository,
            SeatRepository seatRepository,
            OfferRepository offerRepository
    ) {
        this.inventoryGaRepository = inventoryGaRepository;
        this.inventorySeatRepository = inventorySeatRepository;
        this.eventManifestRepository = eventManifestRepository;
        this.gaAreaRepository = gaAreaRepository;
        this.seatRepository = seatRepository;
        this.offerRepository = offerRepository;
    }

    @EventListener
    @Transactional
    public void onEventStatusTransition(EventStatusTransitionEvent event) {
        if (event.getNewStatus() != EventStatus.ONSALE) {
            return;
        }

        UUID eventId = event.getEventId();

        // Get the cloned venue manifest snapshot associated with the event
        eventManifestRepository.findById(eventId).ifPresent(eventManifest -> {
            String manifestId = eventManifest.getManifestId();

            // Load all offers for the event in a deterministic order
            List<Offer> offers = offerRepository.findByEventIdOrderByCreatedAtAsc(eventId);

            // 1. Populate General Admission Areas Inventory (InventoryGa) if not already initialized
            if (!inventoryGaRepository.existsByEventId(eventId)) {
                List<Offer> gaOffers = offers.stream()
                        .filter(o -> o.getSeatingMode() == SeatingMode.GENERAL_ADMISSION)
                        .toList();

                List<GAArea> gaAreas = gaAreaRepository.findByManifestId(manifestId);
                List<InventoryGa> gaInventories = new ArrayList<>();

                for (Offer offer : gaOffers) {
                    List<GAArea> matchingAreas = gaAreas.stream()
                            .filter(area -> (offer.getSectionId() == null || Objects.equals(area.getSectionId(), offer.getSectionId()))
                                    && (offer.getPriceLevelId() == null || Objects.equals(area.getPriceLevelId(), offer.getPriceLevelId())))
                            .toList();

                    if (matchingAreas.isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_MAPPING",
                                "No GA area matches offer " + offer.getTicketTypeId());
                    }

                    for (GAArea area : matchingAreas) {
                        int capacity = offer.getCapacityCap() != null ? offer.getCapacityCap() : area.getCapacity();
                        gaInventories.add(InventoryGa.builder()
                                .eventId(eventId)
                                .areaId(area.getId())
                                .offerId(offer.getId())
                                .total(capacity)
                                .available(capacity)
                                .held(0)
                                .sold(0)
                                .build());
                    }
                }
                if (!gaInventories.isEmpty()) {
                    inventoryGaRepository.saveAll(gaInventories);
                }
            }

            // 2. Populate Reserved Seating Inventory (InventorySeat) if not already initialized
            if (!inventorySeatRepository.existsByEventId(eventId)) {
                List<Offer> rsOffers = offers.stream()
                        .filter(o -> o.getSeatingMode() == SeatingMode.RESERVED_SEATING)
                        .toList();

                // High-performance query using JOIN FETCH to avoid N+1 select queries
                List<Seat> seats = seatRepository.findByManifestIdWithSection(manifestId);
                List<InventorySeat> inventorySeats = new ArrayList<>();
                for (Seat seat : seats) {
                    UUID matchedOfferId = rsOffers.stream()
                            .filter(o -> Objects.equals(o.getSectionId(), seat.getSeatRow().getRsArea().getSectionId())
                                    && Objects.equals(o.getPriceLevelId(), seat.getSeatRow().getRsArea().getPriceLevelId()))
                            .map(Offer::getId)
                            .findFirst()
                            .orElse(null);

                    if (matchedOfferId == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OFFER_MAPPING",
                                "No matching offer found for seat " + seat.getId() + " in section " 
                                + seat.getSeatRow().getRsArea().getSectionId() + " and price level " 
                                + seat.getSeatRow().getRsArea().getPriceLevelId());
                    }

                    inventorySeats.add(InventorySeat.builder()
                            .eventId(eventId)
                            .seatId(seat.getId())
                            .offerId(matchedOfferId)
                            .status(SeatInventoryStatus.AVAILABLE)
                            .build());
                }
                if (!inventorySeats.isEmpty()) {
                    inventorySeatRepository.saveAll(inventorySeats);
                }
            }
        });
    }
}
