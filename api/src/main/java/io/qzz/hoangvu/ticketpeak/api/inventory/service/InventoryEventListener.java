package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatusTransitionEvent;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.GAAreaRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.SeatRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class InventoryEventListener {

    private final GAInventoryRepository gaInventoryRepository;
    private final InventorySeatRepository inventorySeatRepository;
    private final EventManifestRepository eventManifestRepository;
    private final GAAreaRepository gaAreaRepository;
    private final SeatRepository seatRepository;
    private final OfferRepository offerRepository;

    public InventoryEventListener(
            GAInventoryRepository gaInventoryRepository,
            InventorySeatRepository inventorySeatRepository,
            EventManifestRepository eventManifestRepository,
            GAAreaRepository gaAreaRepository,
            SeatRepository seatRepository,
            OfferRepository offerRepository
    ) {
        this.gaInventoryRepository = gaInventoryRepository;
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

        // Ensure idempotency
        if (gaInventoryRepository.existsByEventId(eventId) || inventorySeatRepository.existsByEventId(eventId)) {
            return;
        }

        // Get the cloned venue manifest snapshot associated with the event
        eventManifestRepository.findById(eventId).ifPresent(eventManifest -> {
            String manifestId = eventManifest.getManifestId();

            // Load all offers for the event
            List<Offer> offers = offerRepository.findByEventId(eventId);

            // 1. Populate General Admission Areas Inventory (InventoryGa)
            List<Offer> gaOffers = offers.stream()
                    .filter(o -> o.getSeatingMode() == SeatingMode.GENERAL_ADMISSION)
                    .toList();

            List<GAArea> gaAreas = gaAreaRepository.findByManifestId(manifestId);
            List<InventoryGa> gaInventories = new ArrayList<>();

            for (Offer offer : gaOffers) {
                gaAreas.stream()
                        .filter(area -> area.getSectionId().equals(offer.getSectionId()) 
                                && area.getPriceLevelId().equals(offer.getPriceLevelId()))
                        .findFirst()
                        .ifPresent(area -> {
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
                        });
            }
            gaInventoryRepository.saveAll(gaInventories);

            // 2. Populate Reserved Seating Inventory (InventorySeat)
            List<Offer> rsOffers = offers.stream()
                    .filter(o -> o.getSeatingMode() == SeatingMode.RESERVED_SEATING)
                    .toList();

            List<Seat> seats = seatRepository.findByManifestId(manifestId);
            List<InventorySeat> inventorySeats = seats.stream()
                    .map(seat -> {
                        UUID matchedOfferId = rsOffers.stream()
                                .filter(o -> o.getSectionId().equals(seat.getSeatRow().getRsArea().getSectionId())
                                        && o.getPriceLevelId().equals(seat.getSeatRow().getRsArea().getPriceLevelId()))
                                .map(Offer::getId)
                                .findFirst()
                                .orElse(null);

                        return InventorySeat.builder()
                                .eventId(eventId)
                                .seatId(seat.getId())
                                .offerId(matchedOfferId)
                                .status(SeatInventoryStatus.AVAILABLE)
                                .build();
                    })
                    .toList();
            inventorySeatRepository.saveAll(inventorySeats);
        });
    }
}
