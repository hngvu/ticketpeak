package io.qzz.hoangvu.ticketpeak.api.organization.repository;

import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitation;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, Integer> {

    Optional<OrganizationInvitation> findByToken(String token);

    boolean existsByOrganizationIdAndInviteeAccountIdAndStatusIn(UUID organizationId, UUID inviteeAccountId, List<OrganizationInvitationStatus> statuses);

    List<OrganizationInvitation> findByOrganizationId(UUID organizationId);

    List<OrganizationInvitation> findByInviteeAccountId(UUID inviteeAccountId);
}
