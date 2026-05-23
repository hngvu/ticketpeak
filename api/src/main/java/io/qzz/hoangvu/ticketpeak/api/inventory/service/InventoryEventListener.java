package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatusTransitionEvent;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.GAInventory;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.GAAreaRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.SeatRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryEventListener {

    private final GAInventoryRepository gaInventoryRepository;
    private final InventorySeatRepository inventorySeatRepository;
    private final EventManifestRepository eventManifestRepository;
    private final GAAreaRepository gaAreaRepository;
    private final SeatRepository seatRepository;

    public InventoryEventListener(
            GAInventoryRepository gaInventoryRepository,
            InventorySeatRepository inventorySeatRepository,
            EventManifestRepository eventManifestRepository,
            GAAreaRepository gaAreaRepository,
            SeatRepository seatRepository
    ) {
        this.gaInventoryRepository = gaInventoryRepository;
        this.inventorySeatRepository = inventorySeatRepository;
        this.eventManifestRepository = eventManifestRepository;
        this.gaAreaRepository = gaAreaRepository;
        this.seatRepository = seatRepository;
    }

    @EventListener
    @Transactional
    public void onEventStatusTransition(EventStatusTransitionEvent event) {
        if (event.getNewStatus() != EventStatus.ONSALE) {
            return;
        }

        UUID eventId = event.getEventId();

        // Ensure idempotency: check if inventory already exists for this event
        if (gaInventoryRepository.existsByEventId(eventId) || inventorySeatRepository.existsByEventId(eventId)) {
            return;
        }

        // Get the cloned venue manifest snapshot associated with the event
        eventManifestRepository.findById(eventId).ifPresent(eventManifest -> {
            String manifestId = eventManifest.getManifestId();

            // 1. Populate General Admission Areas Inventory in bulk
            List<GAArea> gaAreas = gaAreaRepository.findByManifestId(manifestId);
            List<GAInventory> gaInventories = gaAreas.stream()
                    .map(area -> GAInventory.builder()
                            .eventId(eventId)
                            .areaId(area.getId())
                            .capacity(area.getCapacity())
                            .sold(0)
                            .build())
                    .toList();
            gaInventoryRepository.saveAll(gaInventories);

            // 2. Populate Reserved Seating Inventory in bulk
            List<Seat> seats = seatRepository.findByManifestId(manifestId);
            List<InventorySeat> inventorySeats = seats.stream()
                    .map(seat -> InventorySeat.builder()
                            .eventId(eventId)
                            .seatId(seat.getId())
                            .status("AVAILABLE")
                            .build())
                    .toList();
            inventorySeatRepository.saveAll(inventorySeats);
        });
    }
}
