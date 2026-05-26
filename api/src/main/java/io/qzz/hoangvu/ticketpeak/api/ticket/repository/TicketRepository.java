package io.qzz.hoangvu.ticketpeak.api.ticket.repository;

import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByIdAndAccountId(UUID id, UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") UUID id);

    List<Ticket> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    Page<Ticket> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
