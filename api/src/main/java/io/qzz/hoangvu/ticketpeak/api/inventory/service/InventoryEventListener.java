package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatusTransitionEvent;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.GAInventory;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.GAAreaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryEventListener {

    private final GAInventoryRepository gaInventoryRepository;
    private final EventManifestRepository eventManifestRepository;
    private final GAAreaRepository gaAreaRepository;

    public InventoryEventListener(
            GAInventoryRepository gaInventoryRepository,
            EventManifestRepository eventManifestRepository,
            GAAreaRepository gaAreaRepository
    ) {
        this.gaInventoryRepository = gaInventoryRepository;
        this.eventManifestRepository = eventManifestRepository;
        this.gaAreaRepository = gaAreaRepository;
    }

    @EventListener
    @Transactional
    public void onEventStatusTransition(EventStatusTransitionEvent event) {
        if (event.getNewStatus() != EventStatus.ONSALE) {
            return;
        }

        UUID eventId = event.getEventId();

        // Ensure idempotency: check if inventory already exists for this event
        if (gaInventoryRepository.existsByEventId(eventId)) {
            return;
        }

        // Get the cloned venue manifest snapshot associated with the event
        eventManifestRepository.findById(eventId).ifPresent(eventManifest -> {
            String manifestId = eventManifest.getManifestId();

            // Fetch General Admission areas for the venue manifest
            List<GAArea> gaAreas = gaAreaRepository.findByManifestId(manifestId);

            // Populate GA inventories using bulk operations
            List<GAInventory> gaInventories = gaAreas.stream()
                    .map(area -> GAInventory.builder()
                            .eventId(eventId)
                            .areaId(area.getId())
                            .capacity(area.getCapacity())
                            .held(0)
                            .purchased(0)
                            .build())
                    .toList();

            gaInventoryRepository.saveAll(gaInventories);
        });
    }
}
