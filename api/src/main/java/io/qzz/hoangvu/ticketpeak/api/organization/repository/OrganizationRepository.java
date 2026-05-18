package io.qzz.hoangvu.ticketpeak.api.organization.repository;

import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
