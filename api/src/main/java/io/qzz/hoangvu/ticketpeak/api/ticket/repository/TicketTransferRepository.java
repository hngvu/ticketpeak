package io.qzz.hoangvu.ticketpeak.api.ticket.repository;

import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransfer;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTransferRepository extends JpaRepository<TicketTransfer, UUID> {

    Optional<TicketTransfer> findByTicketIdAndStatus(UUID ticketId, TicketTransferStatus status);

    List<TicketTransfer> findByRecipientIdAndStatus(UUID recipientId, TicketTransferStatus status);
}
