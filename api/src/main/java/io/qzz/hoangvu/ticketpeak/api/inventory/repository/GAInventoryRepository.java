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

    @Modifying(clearAutomatically = true)
    @Query("UPDATE GAInventory g SET g.sold = g.sold + :qty " +
           "WHERE g.eventId = :eventId AND g.areaId = :areaId AND (g.capacity - g.sold) >= :qty")
    int sellGAInventory(@Param("eventId") UUID eventId, @Param("areaId") String areaId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE GAInventory g SET g.sold = g.sold - :qty " +
           "WHERE g.eventId = :eventId AND g.areaId = :areaId AND g.sold >= :qty")
    int refundGAInventory(@Param("eventId") UUID eventId, @Param("areaId") String areaId, @Param("qty") int qty);
}
