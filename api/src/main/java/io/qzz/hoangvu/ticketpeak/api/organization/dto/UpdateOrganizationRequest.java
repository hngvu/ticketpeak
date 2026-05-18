package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationRequest(
        @NotBlank String name,
        String bio,
        String logoUrl,
        String websiteUrl,
        String email,
        String phone,
        Integer cityId,
        String countryCode
) {
}
