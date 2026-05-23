package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryHoldPlace;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryHoldPlaceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InventoryCleanupService {

    private final InventoryHoldPlaceRepository inventoryHoldPlaceRepository;
    private final GAInventoryRepository gaInventoryRepository;

    public InventoryCleanupService(
            InventoryHoldPlaceRepository inventoryHoldPlaceRepository,
            GAInventoryRepository gaInventoryRepository
    ) {
        this.inventoryHoldPlaceRepository = inventoryHoldPlaceRepository;
        this.gaInventoryRepository = gaInventoryRepository;
    }

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void cleanupExpiredHolds() {
        Instant now = Instant.now();
        List<InventoryHoldPlace> expiredHolds = inventoryHoldPlaceRepository.findAllByExpiresAtBefore(now);
        if (expiredHolds.isEmpty()) {
            return;
        }

        for (InventoryHoldPlace hold : expiredHolds) {
            // For General Admission, restore held capacity atomically
            if (hold.getAreaId() != null) {
                gaInventoryRepository.releaseGAInventory(hold.getEventId(), hold.getAreaId(), hold.getQuantity());
            }
            // For Reserved Seating, seat remains AVAILABLE in seat table, so no action is required
        }

        // Bulk delete expired holds
        inventoryHoldPlaceRepository.deleteByExpiresAtBefore(now);
    }
}
