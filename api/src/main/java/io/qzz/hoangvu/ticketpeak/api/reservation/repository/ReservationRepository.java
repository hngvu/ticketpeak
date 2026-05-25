package io.qzz.hoangvu.ticketpeak.api.reservation.repository;

import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    org.springframework.data.domain.Page<Reservation> findByAccountIdOrderByCreatedAtDesc(UUID accountId, org.springframework.data.domain.Pageable pageable);

    Optional<Reservation> findByIdAndAccountId(UUID id, UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);


    /**
     * Load a reservation with a pessimistic write lock, scoped to the owning account.
     * Used by confirm and cancel to prevent races with the expiry scheduler.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id AND r.accountId = :accountId")
    Optional<Reservation> findByIdAndAccountIdForUpdate(@Param("id") UUID id,
                                                         @Param("accountId") UUID accountId);

    /**
     * Fetch a batch of PENDING reservations whose TTL has elapsed.
     * Uses SKIP LOCKED so concurrent scheduler runs don't block each other.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT r FROM Reservation r
            WHERE r.status = :status
              AND r.expiresAt < :now
            ORDER BY r.expiresAt ASC
            LIMIT :batchSize
            """)
    List<Reservation> findExpiredPendingForUpdate(@Param("status") ReservationStatus status,
                                                   @Param("now") Instant now,
                                                   @Param("batchSize") int batchSize);

    /**
     * Guard against duplicate active RS holds for the same seat.
     */
    @Query("""
            SELECT COUNT(i) > 0 FROM ReservationItem i
            JOIN i.reservation r
            WHERE r.eventId = :eventId
              AND i.seatId  = :seatId
              AND r.status  = 'PENDING'
            """)
    boolean existsActiveSeatReservation(@Param("eventId") UUID eventId,
                                         @Param("seatId") String seatId);

    List<Reservation> findByStatusAndUpdatedAtBefore(ReservationStatus status, Instant threshold, org.springframework.data.domain.Pageable pageable);
}
