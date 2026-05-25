package io.qzz.hoangvu.ticketpeak.api.reservation.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferSaleWindow;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.dto.CreateReservationRequest;
import io.qzz.hoangvu.ticketpeak.api.reservation.dto.ReservationItemRequest;
import io.qzz.hoangvu.ticketpeak.api.reservation.dto.ReservationResponse;
import io.qzz.hoangvu.ticketpeak.api.reservation.exception.ReservationException;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationItem;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final List<EventStatus> RESERVABLE_STATUSES = List.of(EventStatus.ONSALE);

    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final OfferRepository offerRepository;
    private final EventRepository eventRepository;
    private final long ttlMinutes;

    public ReservationService(
            ReservationRepository reservationRepository,
            InventoryService inventoryService,
            OfferRepository offerRepository,
            EventRepository eventRepository,
            @Value("${reservation.ttl-minutes:15}") long ttlMinutes
    ) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.offerRepository = offerRepository;
        this.eventRepository = eventRepository;
        this.ttlMinutes = ttlMinutes;
    }

    // ─── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public ReservationResponse createReservation(UUID accountId, CreateReservationRequest request) {
        // Intentionally same error for both "not found" and "wrong status"
        // to avoid disclosing event existence to unauthorized buyers
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(ReservationException::eventNotOnSale);

        if (!RESERVABLE_STATUSES.contains(event.getStatus())) {
            throw ReservationException.eventNotOnSale();
        }

        Instant now = Instant.now();
        List<ReservationItem> items = new ArrayList<>();
        String reservationCurrency = null;

        Reservation reservation = Reservation.builder()
                .accountId(accountId)
                .eventId(event.getId())
                .status(ReservationStatus.PENDING)
                .currency("") // set after first item
                .expiresAt(now.plusSeconds(ttlMinutes * 60))
                .items(items)
                .build();

        for (ReservationItemRequest itemReq : request.items()) {
            Offer offer = offerRepository.findById(itemReq.offerId())
                    .filter(o -> o.getEventId().equals(event.getId()))
                    .orElseThrow(ReservationException::offerNotFound);

            // Currency consistency across all items
            if (reservationCurrency == null) {
                reservationCurrency = offer.getCurrency();
            } else if (!reservationCurrency.equals(offer.getCurrency())) {
                throw ReservationException.currencyMismatch();
            }

            // Verify offer has an active sale window
            if (!isInActiveSaleWindow(offer, now)) {
                throw ReservationException.offerNotInSaleWindow();
            }

            // Validate seating mode matches offer
            if (offer.getSeatingMode() != itemReq.seatingMode()) {
                throw ReservationException.invalidItem(
                        "Seating mode mismatch for offer " + offer.getId());
            }

            ReservationItem item = buildItem(reservation, offer, itemReq);

            if (itemReq.seatingMode() == SeatingMode.GENERAL_ADMISSION) {
                validateGaItem(itemReq, offer);
                try {
                    inventoryService.holdGAInventory(event.getId(), itemReq.areaId(), offer.getId(), itemReq.qty());
                } catch (ApiException e) {
                    throw ReservationException.gaCapacityInsufficient(itemReq.areaId());
                }
            } else {
                validateRsItem(itemReq);
                if (reservationRepository.existsActiveSeatReservation(event.getId(), itemReq.seatId())) {
                    throw ReservationException.invalidItem(
                            "Seat " + itemReq.seatId() + " is already held by another reservation");
                }
                try {
                    inventoryService.holdSeat(event.getId(), itemReq.seatId());
                } catch (ApiException e) {
                    throw ReservationException.seatUnavailable(itemReq.seatId());
                }
            }

            items.add(item);
        }

        reservation.setCurrency(reservationCurrency);
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    // ─── Confirm ─────────────────────────────────────────────────────────────

    @Transactional
    public ReservationResponse confirmReservation(UUID accountId, UUID reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndAccountIdForUpdate(reservationId, accountId)
                .orElseThrow(ReservationException::notFound);

        assertPending(reservation);

        if (Instant.now().isAfter(reservation.getExpiresAt())) {
            expireSingle(reservation);
            Reservation saved = reservationRepository.saveAndFlush(reservation);
            return ReservationResponse.from(saved);
        }

        for (ReservationItem item : reservation.getItems()) {
            if (item.getSeatingMode() == SeatingMode.GENERAL_ADMISSION) {
                try {
                    inventoryService.confirmGAInventory(
                            reservation.getEventId(), item.getAreaId(), item.getOfferId(), item.getQty());
                } catch (ApiException e) {
                    throw ReservationException.gaCapacityInsufficient(item.getAreaId());
                }
            } else {
                try {
                    inventoryService.sellSeat(reservation.getEventId(), item.getSeatId());
                } catch (ApiException e) {
                    throw ReservationException.seatUnavailable(item.getSeatId());
                }
            }
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.saveAndFlush(reservation);
        return ReservationResponse.from(saved);
    }

    // ─── Cancel ──────────────────────────────────────────────────────────────

    @Transactional
    public ReservationResponse cancelReservation(UUID accountId, UUID reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndAccountIdForUpdate(reservationId, accountId)
                .orElseThrow(ReservationException::notFound);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ReservationException.alreadyFinalized();
        }

        releaseHolds(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.saveAndFlush(reservation);
        return ReservationResponse.from(saved);
    }

    // ─── Read ────────────────────────────────────────────────────────────────

    public ReservationResponse getReservation(UUID accountId, UUID reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndAccountId(reservationId, accountId)
                .orElseThrow(ReservationException::notFound);
        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> listReservations(UUID accountId) {
        return reservationRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // ─── Expiry batch (called by scheduler) ──────────────────────────────────

    @Transactional
    public void expireBatch(int batchSize) {
        List<Reservation> stale = reservationRepository.findExpiredPendingForUpdate(
                ReservationStatus.PENDING, Instant.now(), batchSize);
        for (Reservation r : stale) {
            expireSingle(r);
            reservationRepository.save(r);
        }
        reservationRepository.flush();
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    private void expireSingle(Reservation reservation) {
        releaseHolds(reservation);
        reservation.setStatus(ReservationStatus.EXPIRED);
    }

    private void releaseHolds(Reservation reservation) {
        for (ReservationItem item : reservation.getItems()) {
            if (item.getSeatingMode() == SeatingMode.GENERAL_ADMISSION) {
                inventoryService.releaseGAInventory(
                        reservation.getEventId(), item.getAreaId(), item.getOfferId(), item.getQty());
            } else {
                inventoryService.releaseSeat(reservation.getEventId(), item.getSeatId());
            }
        }
    }

    private void assertPending(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ReservationException.alreadyFinalized();
        }
    }

    private boolean isInActiveSaleWindow(Offer offer, Instant now) {
        List<OfferSaleWindow> windows = offer.getSaleWindows();
        if (windows == null || windows.isEmpty()) {
            return false;
        }
        return windows.stream().anyMatch(w -> !now.isBefore(w.getStartAt()) && !now.isAfter(w.getEndAt()));
    }

    private void validateGaItem(ReservationItemRequest req, Offer offer) {
        if (req.areaId() == null || req.areaId().isBlank()) {
            throw ReservationException.invalidItem("areaId is required for GENERAL_ADMISSION items");
        }
        if (req.qty() == null || req.qty() < 1) {
            throw ReservationException.invalidQuantity("qty must be at least 1 for GENERAL_ADMISSION items");
        }
        if (!offer.getSellableQuantities().contains(req.qty())) {
            throw ReservationException.invalidQuantity(
                    "qty " + req.qty() + " is not in the offer's sellable quantities");
        }
    }

    private void validateRsItem(ReservationItemRequest req) {
        if (req.seatId() == null || req.seatId().isBlank()) {
            throw ReservationException.invalidItem("seatId is required for RESERVED_SEATING items");
        }
    }

    private ReservationItem buildItem(Reservation reservation, Offer offer, ReservationItemRequest req) {
        int qty = req.seatingMode() == SeatingMode.RESERVED_SEATING ? 1 : req.qty();
        return ReservationItem.builder()
                .reservation(reservation)
                .offerId(offer.getId())
                .seatingMode(req.seatingMode())
                .areaId(req.areaId())
                .seatId(req.seatId())
                .qty(qty)
                .unitFaceValue(offer.getFaceValue())
                .currency(offer.getCurrency())
                .charges(offer.getCharges())
                .build();
    }
}
