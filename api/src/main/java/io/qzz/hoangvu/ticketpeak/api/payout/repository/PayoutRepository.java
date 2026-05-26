package io.qzz.hoangvu.ticketpeak.api.payout.repository;

import io.qzz.hoangvu.ticketpeak.api.payout.model.Payout;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    Page<Payout> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);
    
    Page<Payout> findByStatusAndScheduledAfterLessThanEqual(PayoutStatus status, Instant before, Pageable pageable);

    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);
    
    List<Payout> findByPayoutMethodIdAndStatusIn(UUID payoutMethodId, List<PayoutStatus> statuses);
    
    Optional<Payout> findByResaleListingId(UUID resaleListingId);

    @Query("SELECT p.id FROM Payout p WHERE p.status = :status AND p.scheduledAfter <= :before")
    List<UUID> findIdsByStatusAndScheduledAfterLessThanEqual(@Param("status") PayoutStatus status, @Param("before") Instant before);
}
