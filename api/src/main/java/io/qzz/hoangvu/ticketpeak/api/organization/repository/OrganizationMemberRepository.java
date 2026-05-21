package io.qzz.hoangvu.ticketpeak.api.organization.repository;

import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Integer> {

    Optional<OrganizationMember> findByOrganizationIdAndAccountId(UUID organizationId, UUID accountId);

    boolean existsByOrganizationIdAndAccountIdAndStatus(UUID organizationId, UUID accountId, OrganizationMemberStatus status);

    java.util.List<OrganizationMember> findByOrganizationId(UUID organizationId);

    java.util.List<OrganizationMember> findByOrganizationIdAndStatus(UUID organizationId, OrganizationMemberStatus status);

    java.util.List<OrganizationMember> findByAccountIdAndStatus(UUID accountId, OrganizationMemberStatus status);
}
