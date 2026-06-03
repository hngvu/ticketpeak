package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record CreateOrganizationRequest(
        @NotBlank String name,
        @NotBlank @Email String ownerEmail,
        String bio,
        String logoUrl,
        String websiteUrl,
        String email,
        String phone,
        Integer cityId,
        String countryCode
) {
}
