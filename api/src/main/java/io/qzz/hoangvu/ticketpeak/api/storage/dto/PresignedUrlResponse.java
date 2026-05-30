package io.qzz.hoangvu.ticketpeak.api.storage.dto;

public record PresignedUrlResponse(
    String uploadUrl,
    String downloadUrl,
    String fileKey
) {}
