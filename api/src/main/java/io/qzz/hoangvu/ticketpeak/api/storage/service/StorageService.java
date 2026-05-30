package io.qzz.hoangvu.ticketpeak.api.storage.service;

import io.qzz.hoangvu.ticketpeak.api.storage.dto.PresignedUrlResponse;
import io.qzz.hoangvu.ticketpeak.api.storage.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.endpoint}")
    private String s3Endpoint;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    public PresignedUrlResponse generatePresignedUploadUrl(String fileName, String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw StorageException.invalidFileType();
        }

        try {
            String sanitizedName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String uniqueKey = UUID.randomUUID() + "-" + sanitizedName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

            String downloadUrl;
            if (s3Endpoint.contains("localhost") || s3Endpoint.contains("127.0.0.1") || s3Endpoint.contains("minio")) {
                downloadUrl = String.format("%s/%s/%s", s3Endpoint, bucketName, uniqueKey);
            } else {
                downloadUrl = String.format("%s/%s", s3Endpoint, uniqueKey);
            }

            return new PresignedUrlResponse(
                    presigned.url().toString(),
                    downloadUrl,
                    uniqueKey
            );
        } catch (Exception ex) {
            throw StorageException.presignedUrlGenerationFailed(ex.getMessage());
        }
    }
}
