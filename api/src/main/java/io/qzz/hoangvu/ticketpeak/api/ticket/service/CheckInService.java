package io.qzz.hoangvu.ticketpeak.api.ticket.service;

import io.qzz.hoangvu.ticketpeak.api.ticket.dto.CheckInRequest;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.CheckInResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.exception.TicketException;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final TicketRepository ticketRepository;
    private final TotpService totpService;

    @Transactional
    public CheckInResponse checkIn(UUID staffAccountId, UUID eventId, CheckInRequest request) {
        String[] parts = request.qrPayload().split(":");
        if (parts.length != 2) throw TicketException.invalidQrFormat();

        UUID ticketId;
        try {
            ticketId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            throw TicketException.invalidQrFormat();
        }
        String otpFromScanner = parts[1];

        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(TicketException::notFound);

        if (!ticket.getEventId().equals(eventId)) throw TicketException.wrongEvent();
        if (ticket.getStatus() == TicketStatus.VOID) throw TicketException.voided();
        if (ticket.getStatus() == TicketStatus.CHECKED_IN) {
            return CheckInResponse.alreadyCheckedIn(ticket);
        }
        if (ticket.getStatus() == TicketStatus.TRANSFERRED) throw TicketException.pendingTransfer();

        if (!totpService.verifyOtp(ticket.getTotpSecretEnc(), otpFromScanner)) {
            throw TicketException.invalidOtp();
        }

        ticket.setStatus(TicketStatus.CHECKED_IN);
        ticket.setCheckedInAt(Instant.now());
        ticketRepository.save(ticket);

        return CheckInResponse.success(ticket);
    }

    @Transactional(readOnly = true)
    public java.util.List<io.qzz.hoangvu.ticketpeak.api.ticket.dto.SimulatedTicketResponse> getTicketsForSimulation(UUID eventId) {
        java.util.List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        return tickets.stream().map(t -> {
            String otp = "";
            try {
                otp = totpService.generateOtp(t.getTotpSecretEnc());
            } catch (Exception e) {
                // Fallback for missing/empty OTP
            }
            String payload = t.getId().toString() + ":" + otp;
            return new io.qzz.hoangvu.ticketpeak.api.ticket.dto.SimulatedTicketResponse(
                    t.getId(),
                    t.getOfferName(),
                    t.getSeatId(),
                    t.getStatus().name(),
                    otp,
                    payload
            );
        }).toList();
    }
}
