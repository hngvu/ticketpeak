package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.*;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.GAInventory;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryHoldPlace;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryHoldPlaceRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.SeatRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final GAInventoryRepository gaInventoryRepository;
    private final InventoryHoldPlaceRepository inventoryHoldPlaceRepository;
    private final EventRepository eventRepository;
    private final EventManifestRepository eventManifestRepository;
    private final SeatRepository seatRepository;

    public InventoryService(
            GAInventoryRepository gaInventoryRepository,
            InventoryHoldPlaceRepository inventoryHoldPlaceRepository,
            EventRepository eventRepository,
            EventManifestRepository eventManifestRepository,
            SeatRepository seatRepository
    ) {
        this.gaInventoryRepository = gaInventoryRepository;
        this.inventoryHoldPlaceRepository = inventoryHoldPlaceRepository;
        this.eventRepository = eventRepository;
        this.eventManifestRepository = eventManifestRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public HoldInventoryResponse requestHolds(HoldInventoryRequest req) {
        UUID eventId = req.eventId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found"));

        if (event.getStatus() != EventStatus.ONSALE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EVENT_NOT_ONSALE", "Tickets are not on sale for this event");
        }

        String holdToken = req.holdToken();
        if (holdToken == null || holdToken.isBlank()) {
            holdToken = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // Holds expire in 10 minutes
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(10));

        List<String> heldSeatIds = new ArrayList<>();
        List<HoldInventoryResponse.GAHoldDetail> heldGAHolds = new ArrayList<>();

        // Process Reserved seat holds
        if (req.seatIds() != null && !req.seatIds().isEmpty()) {
            for (String seatId : req.seatIds()) {
                // Lazily check if there is an active hold
                Optional<InventoryHoldPlace> activeHoldOpt = inventoryHoldPlaceRepository.findByEventIdAndSeatId(eventId, seatId);
                if (activeHoldOpt.isPresent()) {
                    InventoryHoldPlace existingHold = activeHoldOpt.get();
                    if (existingHold.getExpiresAt().isBefore(Instant.now())) {
                        inventoryHoldPlaceRepository.delete(existingHold);
                    } else {
                        throw new ApiException(HttpStatus.CONFLICT, "SEAT_ALREADY_HELD", "Seat " + seatId + " is already held");
                    }
                }

                // Verify the seat is actually available in venue database (not sold / reserved)
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND", "Seat " + seatId + " does not exist"));

                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new ApiException(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", "Seat " + seatId + " is not available");
                }

                InventoryHoldPlace hold = InventoryHoldPlace.builder()
                        .eventId(eventId)
                        .seatId(seatId)
                        .quantity(1)
                        .holdToken(holdToken)
                        .expiresAt(expiresAt)
                        .build();

                try {
                    inventoryHoldPlaceRepository.saveAndFlush(hold);
                    heldSeatIds.add(seatId);
                } catch (DataIntegrityViolationException ex) {
                    throw new ApiException(HttpStatus.CONFLICT, "SEAT_ALREADY_HELD", "Seat " + seatId + " is already held");
                }
            }
        }

        // Process GA capacity holds
        if (req.gaHolds() != null && !req.gaHolds().isEmpty()) {
            for (GAHoldRequest gaHoldReq : req.gaHolds()) {
                String areaId = gaHoldReq.areaId();
                int qty = gaHoldReq.quantity();

                // Atomically update capacity
                int updatedRows = gaInventoryRepository.holdGAInventory(eventId, areaId, qty);
                if (updatedRows == 0) {
                    throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_GA_CAPACITY", "Insufficient capacity in GA area " + areaId);
                }

                InventoryHoldPlace hold = InventoryHoldPlace.builder()
                        .eventId(eventId)
                        .areaId(areaId)
                        .quantity(qty)
                        .holdToken(holdToken)
                        .expiresAt(expiresAt)
                        .build();

                inventoryHoldPlaceRepository.save(hold);
                heldGAHolds.add(new HoldInventoryResponse.GAHoldDetail(areaId, qty));
            }
        }

        return new HoldInventoryResponse(holdToken, expiresAt, eventId, heldSeatIds, heldGAHolds);
    }

    @Transactional
    public void completePurchase(String holdToken) {
        List<InventoryHoldPlace> holds = inventoryHoldPlaceRepository.findByHoldToken(holdToken);
        if (holds.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "HOLD_NOT_FOUND", "No active hold found for token " + holdToken);
        }

        for (InventoryHoldPlace hold : holds) {
            if (hold.getExpiresAt().isBefore(Instant.now())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "HOLD_EXPIRED", "The hold for this checkout has expired");
            }

            if (hold.getSeatId() != null) {
                // Reserved Seating: Mark seat status as RESERVED (permanently occupied) in the snapshot manifest
                Seat seat = seatRepository.findById(hold.getSeatId())
                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SEAT_NOT_FOUND", "Seat not found"));
                seat.setStatus(SeatStatus.RESERVED);
                seatRepository.save(seat);
            } else if (hold.getAreaId() != null) {
                // GA Seating: Atomically transition held count to purchased count
                gaInventoryRepository.purchaseGAInventory(hold.getEventId(), hold.getAreaId(), hold.getQuantity());
            }
        }

        inventoryHoldPlaceRepository.deleteByHoldToken(holdToken);
    }

    @Transactional(readOnly = true)
    public EventInventoryStatusResponse getAvailability(UUID eventId) {
        // Fetch all active holds for this event
        List<InventoryHoldPlace> activeHolds = inventoryHoldPlaceRepository.findByEventId(eventId).stream()
                .filter(hold -> hold.getExpiresAt().isAfter(Instant.now()))
                .toList();

        // 1. Reserved seating statuses
        var manifestOpt = eventManifestRepository.findById(eventId);
        List<EventInventoryStatusResponse.ReservedSeatStatus> reservedSeatStatuses = new ArrayList<>();
        if (manifestOpt.isPresent()) {
            String manifestId = manifestOpt.get().getManifestId();
            List<Seat> seats = seatRepository.findByManifestId(manifestId);

            Map<String, String> heldSeatMap = activeHolds.stream()
                    .filter(h -> h.getSeatId() != null)
                    .collect(Collectors.toMap(InventoryHoldPlace::getSeatId, h -> "HELD"));

            for (Seat seat : seats) {
                String status = "AVAILABLE";
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    status = "SOLD";
                } else if (heldSeatMap.containsKey(seat.getId())) {
                    status = "HELD";
                }
                reservedSeatStatuses.add(new EventInventoryStatusResponse.ReservedSeatStatus(seat.getId(), status));
            }
        }

        // 2. GA seating statuses
        List<GAInventory> gaInventories = gaInventoryRepository.findByEventId(eventId);
        Map<String, Integer> activeHoldsByGAArea = activeHolds.stream()
                .filter(h -> h.getAreaId() != null)
                .collect(Collectors.groupingBy(
                        InventoryHoldPlace::getAreaId,
                        Collectors.summingInt(InventoryHoldPlace::getQuantity)
                ));

        List<EventInventoryStatusResponse.GAInventoryStatus> gaStatuses = gaInventories.stream()
                .map(ga -> {
                    // Force alignment with real-time active holds
                    int activeHeld = activeHoldsByGAArea.getOrDefault(ga.getAreaId(), 0);
                    int available = Math.max(0, ga.getCapacity() - activeHeld - ga.getPurchased());
                    return new EventInventoryStatusResponse.GAInventoryStatus(
                            ga.getAreaId(),
                            ga.getCapacity(),
                            activeHeld,
                            ga.getPurchased(),
                            available
                    );
                })
                .toList();

        return new EventInventoryStatusResponse(eventId, gaStatuses, reservedSeatStatuses);
    }
}
