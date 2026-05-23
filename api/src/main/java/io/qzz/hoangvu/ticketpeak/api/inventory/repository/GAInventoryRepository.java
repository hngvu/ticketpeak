package io.qzz.hoangvu.ticketpeak.api.inventory.repository;

import io.qzz.hoangvu.ticketpeak.api.inventory.model.GAInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GAInventoryRepository extends JpaRepository<GAInventory, UUID> {

    Optional<GAInventory> findByEventIdAndAreaId(UUID eventId, String areaId);

    List<GAInventory> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    @Modifying
    @Query("UPDATE GAInventory g SET g.held = g.held + :qty " +
           "WHERE g.eventId = :eventId AND g.areaId = :areaId AND (g.capacity - g.held - g.purchased) >= :qty")
    int holdGAInventory(@Param("eventId") UUID eventId, @Param("areaId") String areaId, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE GAInventory g SET g.held = g.held - :qty " +
           "WHERE g.eventId = :eventId AND g.areaId = :areaId AND g.held >= :qty")
    int releaseGAInventory(@Param("eventId") UUID eventId, @Param("areaId") String areaId, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE GAInventory g SET g.held = g.held - :qty, g.purchased = g.purchased + :qty " +
           "WHERE g.eventId = :eventId AND g.areaId = :areaId AND g.held >= :qty")
    int purchaseGAInventory(@Param("eventId") UUID eventId, @Param("areaId") String areaId, @Param("qty") int qty);
}
