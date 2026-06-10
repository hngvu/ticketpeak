package io.qzz.hoangvu.ticketpeak.api.inventory.service;

import io.qzz.hoangvu.ticketpeak.api.event.exception.EventException;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.EventInventoryStatusResponse;
import io.qzz.hoangvu.ticketpeak.api.inventory.exception.InventoryException;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryGaRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryGaRepository inventoryGaRepository;
    private final InventorySeatRepository inventorySeatRepository;
    private final EventRepository eventRepository;

    public InventoryService(
            InventoryGaRepository inventoryGaRepository,
            InventorySeatRepository inventorySeatRepository,
            EventRepository eventRepository
    ) {
        this.inventoryGaRepository = inventoryGaRepository;
        this.inventorySeatRepository = inventorySeatRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public EventInventoryStatusResponse getAvailability(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw EventException.notFound();
        }
        if (!inventoryGaRepository.existsByEventId(eventId) && !inventorySeatRepository.existsByEventId(eventId)) {
            throw InventoryException.inventoryNotInitialized();
        }

        // GA inventory read path
        List<InventoryGa> gaInventories = inventoryGaRepository.findByEventId(eventId);
        List<EventInventoryStatusResponse.GAInventoryStatus> gaStatuses = gaInventories.stream()
                .map(ga -> new EventInventoryStatusResponse.GAInventoryStatus(
                        ga.getSectionId(),
                        ga.getOfferId(),
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
    public void holdGAInventory(UUID eventId, String sectionId, UUID offerId, int qty) {
        validateQuantity(qty);
        int updated = inventoryGaRepository.holdGa(eventId, sectionId, offerId, qty);
        if (updated == 0) {
            throw InventoryException.insufficientGaCapacity("Insufficient available capacity in GA area " + sectionId);
        }
    }

    @Transactional
    public void releaseGAInventory(UUID eventId, String sectionId, UUID offerId, int qty) {
        validateQuantity(qty);
        int updated = inventoryGaRepository.releaseGa(eventId, sectionId, offerId, qty);
        if (updated == 0) {
            throw InventoryException.invalidRelease("Cannot release held capacity in GA area " + sectionId);
        }
    }

    @Transactional
    public void directSellGAInventory(UUID eventId, String sectionId, UUID offerId, int qty) {
        validateQuantity(qty);
        int updated = inventoryGaRepository.directSellGa(eventId, sectionId, offerId, qty);
        if (updated == 0) {
            throw InventoryException.insufficientGaCapacity("Insufficient capacity in General Admission area " + sectionId);
        }
    }

    @Transactional
    public void confirmGAInventory(UUID eventId, String sectionId, UUID offerId, int qty) {
        validateQuantity(qty);
        int updated = inventoryGaRepository.confirmGa(eventId, sectionId, offerId, qty);
        if (updated == 0) {
            throw InventoryException.insufficientGaHeld("Insufficient held capacity in General Admission area " + sectionId);
        }
    }

    @Transactional
    public void refundGAInventory(UUID eventId, String sectionId, UUID offerId, int qty) {
        validateQuantity(qty);
        int updated = inventoryGaRepository.refundGa(eventId, sectionId, offerId, qty);
        if (updated == 0) {
            throw InventoryException.invalidRefund("Cannot refund quantity " + qty + " for General Admission area " + sectionId);
        }
    }

    @Transactional
    public void holdSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.holdSeat(eventId, seatId);
        if (updated == 0) {
            throw InventoryException.seatNotAvailable("Seat " + seatId + " is not available for hold");
        }
    }

    @Transactional
    public void releaseSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.releaseSeat(eventId, seatId);
        if (updated == 0) {
            throw InventoryException.invalidRelease("Seat " + seatId + " cannot be released");
        }
    }

    @Transactional
    public void sellSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.sellSeat(eventId, seatId);
        if (updated == 0) {
            throw InventoryException.seatNotAvailable("Seat " + seatId + " is not available for purchase");
        }
    }

    @Transactional
    public void refundSeat(UUID eventId, String seatId) {
        int updated = inventorySeatRepository.refundSeat(eventId, seatId);
        if (updated == 0) {
            throw InventoryException.invalidRefund("Seat " + seatId + " cannot be refunded");
        }
    }

    private void validateQuantity(int qty) {
        if (qty <= 0) {
            throw InventoryException.invalidQuantity();
        }
    }
}
