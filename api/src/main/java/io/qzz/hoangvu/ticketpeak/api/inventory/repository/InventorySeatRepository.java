package io.qzz.hoangvu.ticketpeak.api.inventory.repository;

import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventorySeatRepository extends JpaRepository<InventorySeat, UUID> {

    List<InventorySeat> findByEventId(UUID eventId);

    Optional<InventorySeat> findByEventIdAndSeatId(UUID eventId, String seatId);

    boolean existsByEventId(UUID eventId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventorySeat s SET s.status = 'SOLD' " +
           "WHERE s.eventId = :eventId AND s.seatId = :seatId AND s.status = 'AVAILABLE'")
    int sellSeat(@Param("eventId") UUID eventId, @Param("seatId") String seatId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventorySeat s SET s.status = 'AVAILABLE' " +
           "WHERE s.eventId = :eventId AND s.seatId = :seatId AND s.status = 'SOLD'")
    int refundSeat(@Param("eventId") UUID eventId, @Param("seatId") String seatId);
}
