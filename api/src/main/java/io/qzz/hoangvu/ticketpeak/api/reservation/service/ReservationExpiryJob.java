package io.qzz.hoangvu.ticketpeak.api.reservation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryJob.class);

    private final ReservationService reservationService;
    private final int batchSize;

    public ReservationExpiryJob(
            ReservationService reservationService,
            @Value("${reservation.expiry-job.batch-size:100}") int batchSize
    ) {
        this.reservationService = reservationService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${reservation.expiry-job.delay-ms:60000}")
    public void expireStaleReservations() {
        log.debug("Running reservation expiry sweep (batch={})", batchSize);
        reservationService.expireBatch(batchSize);
    }
}
