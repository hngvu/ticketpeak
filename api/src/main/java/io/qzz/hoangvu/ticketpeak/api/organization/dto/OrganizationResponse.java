package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        String bio,
        String logoUrl,
        String websiteUrl,
        String email,
        String phone,
        Integer cityId,
        String countryCode,
        UUID ownerAccountId,
        OrganizationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrganizationResponse from(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getSlug(),
                org.getBio(),
                org.getLogoUrl(),
                org.getWebsiteUrl(),
                org.getEmail(),
                org.getPhone(),
                org.getCityId(),
                org.getCountryCode(),
                org.getOwnerAccountId(),
                org.getStatus(),
                org.getCreatedAt(),
                org.getUpdatedAt()
        );
    }
}
