package io.qzz.hoangvu.ticketpeak.api.inventory.repository;

import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryHoldPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryHoldPlaceRepository extends JpaRepository<InventoryHoldPlace, UUID> {

    List<InventoryHoldPlace> findByHoldToken(String holdToken);

    void deleteByHoldToken(String holdToken);

    List<InventoryHoldPlace> findAllByExpiresAtBefore(Instant time);

    void deleteByExpiresAtBefore(Instant time);

    Optional<InventoryHoldPlace> findByEventIdAndSeatId(UUID eventId, String seatId);

    List<InventoryHoldPlace> findByEventId(UUID eventId);
}
