package io.qzz.hoangvu.ticketpeak.api.organization.dto;

import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;

public record OrganizationSearchParams(
        String name,
        OrganizationStatus status
) {
}
