package io.qzz.hoangvu.ticketpeak.api.inventory.repository;

import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventorySeatRepository extends JpaRepository<InventorySeat, InventorySeatId> {

    List<InventorySeat> findByEventId(UUID eventId);

    Optional<InventorySeat> findByEventIdAndSeatId(UUID eventId, String seatId);

    boolean existsByEventId(UUID eventId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventorySeat s SET s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.HELD " +
           "WHERE s.eventId = :eventId AND s.seatId = :seatId AND s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.AVAILABLE")
    int holdSeat(@Param("eventId") UUID eventId, @Param("seatId") String seatId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventorySeat s SET s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.AVAILABLE " +
           "WHERE s.eventId = :eventId AND s.seatId = :seatId AND s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.HELD")
    int releaseSeat(@Param("eventId") UUID eventId, @Param("seatId") String seatId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventorySeat s SET s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.SOLD " +
           "WHERE s.eventId = :eventId AND s.seatId = :seatId AND " +
           "(s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.AVAILABLE OR s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.HELD)")
    int sellSeat(@Param("eventId") UUID eventId, @Param("seatId") String seatId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventorySeat s SET s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.AVAILABLE " +
           "WHERE s.eventId = :eventId AND s.seatId = :seatId AND s.status = io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus.SOLD")
    int refundSeat(@Param("eventId") UUID eventId, @Param("seatId") String seatId);
}
