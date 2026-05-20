package io.qzz.hoangvu.ticketpeak.api.event.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.event.dto.ClassificationResponse;
import io.qzz.hoangvu.ticketpeak.api.event.service.ClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classifications")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassificationResponse>>> listClassifications() {
        List<ClassificationResponse> response = classificationService.listClassifications();
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassificationResponse>> getClassification(@PathVariable UUID id) {
        ClassificationResponse response = classificationService.getClassification(id);
        return ResponseEntity.ok(ApiResponse.success(response, "OK"));
    }
}
