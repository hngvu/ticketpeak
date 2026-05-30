# 029 — Presigned S3/MinIO Upload Flow — Web & API

## Description

Implement a secure, modern file upload architecture leveraging **AWS S3 Presigned URLs** to allow the SvelteKit frontend (client) to upload media directly to MinIO / S3 object storage without overloading the Spring Boot application server.

### The Flow:
1. **Frontend requests Presigned URL**: Client sends file metadata (`fileName`, `contentType`) to SvelteKit proxy -> proxies request with auth token to Spring Boot API: `POST /api/storage/presigned-url`.
2. **Backend generates Presigned URL**: S3 Presigner in Spring Boot creates a temporary upload URL (valid for 15 minutes) and a stable download URL. Returns `{ uploadUrl, downloadUrl, fileKey }` to SvelteKit.
3. **Client uploads direct**: Frontend uploads the binary file (`File` object) directly to the returned `uploadUrl` via an HTTP `PUT` request.
4. **DTO submit**: Frontend submits the stable `downloadUrl` as part of the entity's DTO (e.g. Venue image, Attraction image, etc.) in the subsequent REST calls.

---

## Files Liên Quan

### 1. Spring Boot Backend (`api/`)

| File | Thay đổi |
|---|---|
| [NEW] `io/qzz/hoangvu/ticketpeak/api/storage/config/StorageConfig.java` | Khởi tạo bean `S3Presigner` cấu hình bằng `app.s3` properties |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/storage/dto/PresignedUrlRequest.java` | DTO yêu cầu xin URL gồm `fileName` và `contentType` |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/storage/dto/PresignedUrlResponse.java` | DTO phản hồi gồm `uploadUrl`, `downloadUrl`, `fileKey` |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/storage/exception/StorageException.java` | Storage factory exceptions |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/storage/service/StorageService.java` | Logic chính sinh presigned URL, sinh key độc nhất, dự đoán download URL |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/storage/controller/StorageController.java` | Lộ REST endpoint `POST /api/storage/presigned-url` bảo vệ bằng auth |

### 2. SvelteKit Frontend (`web/`)

| File | Thay đổi |
|---|---|
| [NEW] `web/src/routes/api/storage/+server.ts` | SvelteKit API proxy chuyển tiếp JWT cookie thành Authorization Header cho Backend |
| [NEW] `web/src/lib/storage.ts` | Utility helper cho client-side thực hiện chu trình upload trực tiếp |
| [NEW] `web/src/routes/demo/upload/+page.svelte` | Trang UI trình diễn và test upload ảnh tuyệt đẹp lên MinIO |

---

## Technical Specifications

### StorageConfig.java
```java
package io.qzz.hoangvu.ticketpeak.api.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.net.URI;

@Configuration
public class StorageConfig {
    @Value("${app.s3.endpoint}")
    private String endpoint;

    @Value("${app.s3.access-key}")
    private String accessKey;

    @Value("${app.s3.secret-key}")
    private String secretKey;

    @Value("${app.s3.region:ap-southeast-1}")
    private String region;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .build();
    }
}
```

### SvelteKit Client Upload helper (`web/src/lib/storage.ts`)
```typescript
export async function uploadFileDirect(file: File): Promise<{ downloadUrl: string; fileKey: string }> {
	const res = await fetch('/api/storage', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			fileName: file.name,
			contentType: file.type
		})
	});

	if (!res.ok) {
		const json = await res.json().catch(() => ({}));
		throw new Error(json.message || 'Failed to request upload signature.');
	}

	const { data } = await res.json();
	const { uploadUrl, downloadUrl, fileKey } = data;

	const uploadRes = await fetch(uploadUrl, {
		method: 'PUT',
		headers: {
			'Content-Type': file.type
		},
		body: file
	});

	if (!uploadRes.ok) {
		throw new Error('Direct binary upload to MinIO/S3 storage failed.');
	}

	return { downloadUrl, fileKey };
}
```

---

## Acceptance Criteria

- [ ] SvelteKit builds and passes checks (`npm run check` and `npm run lint`) cleanly.
- [ ] Spring Boot backend compiles without errors and handles GraalVM native image compatibility.
- [ ] Requesting `/api/storage/presigned-url` requires active JWT authentication.
- [ ] Direct upload helper works flawlessly by PUTting binary file payloads directly to the generated presigned S3/MinIO URLs.
- [ ] An interactive demonstration upload interface is built under `/demo/upload` with premium layout, file drop support, direct uploading indicators, and instant download preview/URL copy utility.

---

## Status

`planned`

## Outcome

_To be filled after implementation._
