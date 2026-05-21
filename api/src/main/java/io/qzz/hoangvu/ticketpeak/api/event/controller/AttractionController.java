package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.AttractionResponse;
import io.qzz.hoangvu.ticketpeak.api.event.service.AttractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    private final AttractionService attractionService;

    public AttractionController(AttractionService attractionService) {
        this.attractionService = attractionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttractionResponse>>> listAttractions() {
        List<AttractionResponse> response = attractionService.listAttractions();
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttractionResponse>> getAttraction(@PathVariable UUID id) {
        AttractionResponse response = attractionService.getAttraction(id);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }
}
