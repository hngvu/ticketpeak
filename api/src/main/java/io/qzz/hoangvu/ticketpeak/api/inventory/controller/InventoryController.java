package io.qzz.hoangvu.ticketpeak.api.inventory.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.EventInventoryStatusResponse;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.HoldInventoryRequest;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.HoldInventoryResponse;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<HoldInventoryResponse>> requestHolds(
            @Valid @RequestBody HoldInventoryRequest request
    ) {
        HoldInventoryResponse response = inventoryService.requestHolds(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Holds requested successfully"));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<EventInventoryStatusResponse>> getAvailability(
            @PathVariable UUID eventId
    ) {
        EventInventoryStatusResponse response = inventoryService.getAvailability(eventId);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }
}
