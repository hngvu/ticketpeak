package io.qzz.hoangvu.ticketpeak.api.ticket.service;

import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.*;
import io.qzz.hoangvu.ticketpeak.api.ticket.exception.TicketException;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransfer;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransferStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTransferRepository transferRepository;
    private final EventRepository eventRepository;
    private final TotpService totpService;

    // ── Reads ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TicketResponse> listMyTickets(UUID accountId, Pageable pageable) {
        return ticketRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse getMyTicket(UUID accountId, UUID ticketId) {
        Ticket ticket = ticketRepository.findByIdAndAccountId(ticketId, accountId)
                .orElseThrow(TicketException::notFound);
        return TicketResponse.from(ticket);
    }

    // ── QR generation ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TicketQrResponse getQr(UUID accountId, UUID ticketId) {
        Ticket ticket = ticketRepository.findByIdAndAccountId(ticketId, accountId)
                .orElseThrow(TicketException::notFound);

        if (ticket.getStatus() == TicketStatus.VOID) throw TicketException.voided();
        if (ticket.getStatus() == TicketStatus.CHECKED_IN) throw TicketException.alreadyUsed();
        if (ticket.getStatus() == TicketStatus.TRANSFERRED) throw TicketException.pendingTransfer();

        String otp = totpService.generateOtp(ticket.getTotpSecretEnc());
        long windowExpiresAt = totpService.computeWindowExpiry();

        return new TicketQrResponse(ticket.getId(), otp, windowExpiresAt);
    }

    // ── Transfer ──────────────────────────────────────────────────────────

    @Transactional
    public TransferResponse initiateTransfer(UUID senderId, UUID ticketId, InitiateTransferRequest req) {
        Ticket ticket = ticketRepository.findByIdAndAccountId(ticketId, senderId)
                .orElseThrow(TicketException::notFound);

        if (ticket.getStatus() != TicketStatus.ISSUED) throw TicketException.notTransferable();
        if (senderId.equals(req.recipientAccountId())) throw TicketException.cannotTransferToSelf();

        Event event = eventRepository.findById(ticket.getEventId())
                .orElseThrow(TicketException::notFound);
        if (!event.isTransferEnabled()) throw TicketException.transferNotEnabled();

        if (event.getMaxTransferCount() > 0 && ticket.getTransferCount() >= event.getMaxTransferCount()) {
            throw TicketException.transferLimitReached();
        }

        // Cancel any previous pending transfer for this ticket
        transferRepository.findByTicketIdAndStatus(ticketId, TicketTransferStatus.PENDING)
                .ifPresent(t -> {
                    t.setStatus(TicketTransferStatus.CANCELLED);
                    transferRepository.save(t);
                });

        ticket.setStatus(TicketStatus.TRANSFERRED);
        ticketRepository.save(ticket);

        TicketTransfer transfer = TicketTransfer.builder()
                .ticketId(ticketId)
                .senderId(senderId)
                .recipientId(req.recipientAccountId())
                .status(TicketTransferStatus.PENDING)
                .expiresAt(Instant.now().plus(Duration.ofHours(48)))
                .build();

        try {
            transferRepository.save(transfer);
        } catch (DataIntegrityViolationException e) {
            throw TicketException.transferAlreadyPending();
        }
        return TransferResponse.from(transfer);
    }

    @Transactional
    public void acceptTransfer(UUID recipientId, UUID transferId) {
        TicketTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(TicketException::transferNotFound);

        if (!transfer.getRecipientId().equals(recipientId)) throw TicketException.transferNotFound();
        if (transfer.getStatus() != TicketTransferStatus.PENDING) throw TicketException.transferNotPending();
        if (transfer.getExpiresAt().isBefore(Instant.now())) throw TicketException.transferExpired();

        Ticket ticket = ticketRepository.findById(transfer.getTicketId())
                .orElseThrow(TicketException::notFound);

        ticket.setAccountId(recipientId);
        ticket.setStatus(TicketStatus.ISSUED);
        ticket.setTransferCount(ticket.getTransferCount() + 1);
        ticketRepository.save(ticket);

        transfer.setStatus(TicketTransferStatus.ACCEPTED);
        transferRepository.save(transfer);
    }

    @Transactional
    public void cancelTransfer(UUID senderId, UUID transferId) {
        TicketTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(TicketException::transferNotFound);

        if (!transfer.getSenderId().equals(senderId) || transfer.getStatus() != TicketTransferStatus.PENDING) {
            throw TicketException.transferNotFound();
        }

        Ticket ticket = ticketRepository.findById(transfer.getTicketId())
                .orElseThrow(TicketException::notFound);

        ticket.setStatus(TicketStatus.ISSUED);
        ticketRepository.save(ticket);

        transfer.setStatus(TicketTransferStatus.CANCELLED);
        transferRepository.save(transfer);
    }

    // ── Void (called by payment refund hook) ─────────────────────────────

    @Transactional
    public void voidTicketsByOrderId(UUID orderId) {
        List<Ticket> tickets = ticketRepository.findByOrderId(orderId);
        tickets.forEach(t -> t.setStatus(TicketStatus.VOID));
        ticketRepository.saveAll(tickets);
    }
}
