package io.qzz.hoangvu.ticketpeak.api.storage.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.storage.dto.PresignedUrlRequest;
import io.qzz.hoangvu.ticketpeak.api.storage.dto.PresignedUrlResponse;
import io.qzz.hoangvu.ticketpeak.api.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUploadUrl(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = storageService.generatePresignedUploadUrl(
                request.fileName(),
                request.contentType()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Presigned upload URL generated successfully"));
    }
}
