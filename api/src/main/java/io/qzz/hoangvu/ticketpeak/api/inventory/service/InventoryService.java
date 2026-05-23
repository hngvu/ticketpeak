package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.EventInventoryStatusResponse;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final GAInventoryRepository gaInventoryRepository;
    private final InventorySeatRepository inventorySeatRepository;
    private final EventRepository eventRepository;

    public InventoryService(
            GAInventoryRepository gaInventoryRepository,
            InventorySeatRepository inventorySeatRepository,
            EventRepository eventRepository
    ) {
        this.gaInventoryRepository = gaInventoryRepository;
        this.inventorySeatRepository = inventorySeatRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public EventInventoryStatusResponse getAvailability(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
        }

        // GA inventory read path
        List<InventoryGa> gaInventories = gaInventoryRepository.findByEventId(eventId);
        List<EventInventoryStatusResponse.GAInventoryStatus> gaStatuses = gaInventories.stream()
                .map(ga -> new EventInventoryStatusResponse.GAInventoryStatus(
                        ga.getAreaId(),
                        ga.getTotal(),
                        ga.getSold(),
                        ga.getHeld(),
                        ga.getAvailable()
                ))
                .toList();

        // Reserved Seating read path
        List<InventorySeat> inventorySeats = inventorySeatRepository.findByEventId(eventId);
        List<EventInventoryStatusResponse.ReservedSeatStatus> reservedSeatStatuses = inventorySeats.stream()
                .map(seat -> new EventInventoryStatusResponse.ReservedSeatStatus(
                        seat.getSeatId(),
                        seat.getStatus().name()
                ))
                .toList();

        return new EventInventoryStatusResponse(eventId, gaStatuses, reservedSeatStatuses);
    }

    @Transactional
    public void holdGAInventory(UUID eventId, String areaId, UUID offerId, int qty) {
        int updated = gaInventoryRepository.holdGa(eventId, areaId, offerId, qty);
        if (updated == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_GA_CAPACITY", "Insufficient available capacity in GA area " + areaId);
        }
    }

    @Transactional
    public void releaseGAInventory(UUID eventId, String areaId, UUID offerId, int qty) {
        int updated = gaInventoryRepository.releaseGa(eventId, areaId, offerId, qty);
        if (updated == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RELEASE", "Cannot release held capacity in GA area " + areaId);
        }
    }

    @Transactional
    public void sellGAInventory(UUID eventId, String areaId, UUID offerId, int qty) {
        int updated = gaInventoryRepository.sellGAInventory(eventId, areaId, offerId, qty);
        if (updated == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_GA_CAPACITY", "Insufficient capacity in General Admission area " + areaId);
        }
    }

    @Transactional
    public void refundGAInventory(UUID eventId, String areaId, UUID offerId, int qty) {
        int updated = gaInventoryRepository.refundGAInventory(eventId, areaId, offerId, qty);
        if (updated == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REFUND", "Cannot refund quantity " + qty + " for General Admission area " + areaId);
        }
    }

    @Transactional
    public void holdSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.holdSeat(eventId, seatId);
        if (updated == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", "Seat " + seatId + " is not available for hold");
        }
    }

    @Transactional
    public void releaseSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.releaseSeat(eventId, seatId);
        if (updated == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RELEASE", "Seat " + seatId + " cannot be released");
        }
    }

    @Transactional
    public void sellSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.sellSeat(eventId, seatId);
        if (updated == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", "Seat " + seatId + " is not available for purchase");
        }
    }

    @Transactional
    public void refundSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.refundSeat(eventId, seatId);
        if (updated == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REFUND", "Seat " + seatId + " cannot be refunded");
        }
    }
}
