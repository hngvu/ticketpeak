package io.qzz.hoangvu.ticketpeak.api.inventory.repository;

import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryGaRepository extends JpaRepository<InventoryGa, InventoryGaId> {

    List<InventoryGa> findByEventId(UUID eventId);

    Optional<InventoryGa> findByEventIdAndSectionIdAndOfferId(UUID eventId, String sectionId, UUID offerId);

    boolean existsByEventId(UUID eventId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryGa g SET g.held = g.held + :qty, g.available = g.available - :qty " +
           "WHERE :qty > 0 AND g.eventId = :eventId AND g.sectionId = :sectionId AND g.offerId = :offerId AND g.available >= :qty")
    int holdGa(@Param("eventId") UUID eventId, @Param("sectionId") String sectionId, @Param("offerId") UUID offerId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryGa g SET g.held = g.held - :qty, g.available = g.available + :qty " +
           "WHERE :qty > 0 AND g.eventId = :eventId AND g.sectionId = :sectionId AND g.offerId = :offerId AND g.held >= :qty")
    int releaseGa(@Param("eventId") UUID eventId, @Param("sectionId") String sectionId, @Param("offerId") UUID offerId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryGa g SET g.sold = g.sold + :qty, g.available = g.available - :qty " +
           "WHERE :qty > 0 AND g.eventId = :eventId AND g.sectionId = :sectionId AND g.offerId = :offerId AND g.available >= :qty")
    int directSellGa(@Param("eventId") UUID eventId, @Param("sectionId") String sectionId, @Param("offerId") UUID offerId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryGa g SET g.held = g.held - :qty, g.sold = g.sold + :qty " +
           "WHERE :qty > 0 AND g.eventId = :eventId AND g.sectionId = :sectionId AND g.offerId = :offerId AND g.held >= :qty")
    int confirmGa(@Param("eventId") UUID eventId, @Param("sectionId") String sectionId, @Param("offerId") UUID offerId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryGa g SET g.sold = g.sold - :qty, g.available = g.available + :qty " +
           "WHERE :qty > 0 AND g.eventId = :eventId AND g.sectionId = :sectionId AND g.offerId = :offerId AND g.sold >= :qty")
    int refundGa(@Param("eventId") UUID eventId, @Param("sectionId") String sectionId, @Param("offerId") UUID offerId, @Param("qty") int qty);
}
