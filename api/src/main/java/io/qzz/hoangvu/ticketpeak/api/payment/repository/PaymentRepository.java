package io.qzz.hoangvu.ticketpeak.api.payment.repository;

import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndAccountId(UUID id, UUID accountId);

    Optional<Payment> findFirstByReservationIdOrderByCreatedAtDesc(UUID reservationId);

    List<Payment> findByReservationIdAndStatusIn(UUID reservationId, List<PaymentStatus> statuses);

    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    Optional<Payment> findFirstByReservationIdAndStatus(UUID reservationId, PaymentStatus status);
}
