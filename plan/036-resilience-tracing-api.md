# 036-resilience-tracing-api

## Description
Implement OpenTelemetry-based tracing and Resilience4j for external API interactions in the Spring Boot backend.

## Acceptance Criteria
- Dependencies added to pom.xml
- Tracing is enabled via Micrometer OTel bridge (traceId, spanId in logs)
- Resilience4j is configured for external services (Stripe, VNPAY) with Retry and TimeLimiter
- Tests pass

## Status
done

## Outcome
OpenTelemetry Micrometer Tracing and Resilience4j successfully integrated into the backend. 
- Console logs now include traceId and spanId.
- Payment Services (Stripe, VNPay) are guarded by `@Retry`.
