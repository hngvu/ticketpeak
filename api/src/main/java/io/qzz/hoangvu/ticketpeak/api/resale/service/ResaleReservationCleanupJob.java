package io.qzz.hoangvu.ticketpeak.api.resale.service;

import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleReservationCleanupJob {

    private final ResaleListingRepository listingRepository;
    private final ResaleOrderService resaleOrderService;

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void cleanupExpiredReservations() {
        List<UUID> expiredListingIds = listingRepository.findExpiredReservations(ResaleListingStatus.RESERVED, Instant.now());
        
        for (UUID listingId : expiredListingIds) {
            try {
                resaleOrderService.releaseExpiredReservation(listingId);
            } catch (Exception e) {
                log.error("Failed to release expired reservation for listing {}", listingId, e);
            }
        }
    }
}
