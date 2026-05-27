package io.qzz.hoangvu.ticketpeak.api.resale.repository;

import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListing;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResaleListingRepository extends JpaRepository<ResaleListing, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ResaleListing r WHERE r.id = :id")
    Optional<ResaleListing> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT rl FROM ResaleListing rl WHERE rl.ticketId = :ticketId AND rl.status IN :statuses")
    Optional<ResaleListing> findActiveByTicketId(@Param("ticketId") UUID ticketId, @Param("statuses") List<ResaleListingStatus> statuses);

    Page<ResaleListing> findByEventIdAndStatus(UUID eventId, ResaleListingStatus status, Pageable pageable);

    Page<ResaleListing> findBySellerId(UUID sellerId, Pageable pageable);

    int countBySellerIdAndEventIdAndStatusIn(UUID sellerId, UUID eventId, List<ResaleListingStatus> statuses);

    @Query("SELECT r.id FROM ResaleListing r WHERE r.status = :status AND r.reservedUntil < :now")
    List<UUID> findExpiredReservations(@Param("status") ResaleListingStatus status, @Param("now") Instant now);
}
