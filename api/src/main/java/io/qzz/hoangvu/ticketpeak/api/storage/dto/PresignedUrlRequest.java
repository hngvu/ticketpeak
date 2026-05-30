package io.qzz.hoangvu.ticketpeak.api.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
    @NotBlank(message = "File name must not be blank")
    String fileName,

    @NotBlank(message = "Content type must not be blank")
    String contentType
) {}
