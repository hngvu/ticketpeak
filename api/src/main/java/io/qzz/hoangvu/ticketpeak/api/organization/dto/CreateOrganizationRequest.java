package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrganizationRequest(
        @NotBlank String name,
        @NotNull UUID ownerAccountId,
        String bio,
        String logoUrl,
        String websiteUrl,
        String email,
        String phone,
        Integer cityId,
        String countryCode
) {
}
