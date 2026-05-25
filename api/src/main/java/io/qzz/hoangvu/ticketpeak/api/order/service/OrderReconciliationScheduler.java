package io.qzz.hoangvu.ticketpeak.api.order.service;

import io.qzz.hoangvu.ticketpeak.api.payment.event.PaymentCompletedEvent;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class OrderReconciliationScheduler {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderReconciliationScheduler(
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    @Transactional
    public void reconcileOrphanedReservations() {
        log.info("Starting background order reconciliation for orphaned CONFIRMED reservations...");
        Instant threshold = Instant.now().minus(Duration.ofMinutes(5));

        List<Reservation> orphans = reservationRepository.findByStatusAndUpdatedAtBefore(
                ReservationStatus.CONFIRMED, threshold);

        if (orphans.isEmpty()) {
            log.info("No orphaned CONFIRMED reservations found.");
            return;
        }

        log.info("Found {} orphaned CONFIRMED reservations. Initiating self-healing recovery...", orphans.size());

        for (Reservation reservation : orphans) {
            try {
                Payment payment = paymentRepository.findFirstByReservationIdAndStatus(
                        reservation.getId(), PaymentStatus.COMPLETED)
                        .orElseThrow(() -> new IllegalStateException(
                                "Completed payment not found for confirmed reservation " + reservation.getId()));

                log.info("Re-publishing PaymentCompletedEvent for reservation {} (payment {})",
                        reservation.getId(), payment.getId());

                eventPublisher.publishEvent(new PaymentCompletedEvent(
                        this,
                        payment.getId(),
                        reservation.getId(),
                        reservation.getEventId(),
                        reservation.getAccountId(),
                        payment.getAmount(),
                        payment.getCurrency()
                ));

            } catch (Exception e) {
                log.error("Failed to reconcile reservation " + reservation.getId(), e);
            }
        }
    }
}
