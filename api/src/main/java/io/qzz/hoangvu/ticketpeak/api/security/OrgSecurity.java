package io.qzz.hoangvu.ticketpeak.api.security;

import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("orgSecurity")
public class OrgSecurity {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public OrgSecurity(OrganizationRepository organizationRepository, OrganizationMemberRepository organizationMemberRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    public boolean isOwner(UUID organizationId) {
        if (organizationId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        
        if (!(auth.getPrincipal() instanceof AuthenticatedAccount acc)) {
            return false;
        }

        try {
            return organizationRepository.findById(organizationId)
                    .map(org -> org.getOwnerAccountId().equals(acc.accountId()))
                    .orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isMember(UUID organizationId) {
        if (organizationId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }

        if (!(auth.getPrincipal() instanceof AuthenticatedAccount acc)) {
            return false;
        }

        try {
            return organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(
                    organizationId, acc.accountId(), OrganizationMemberStatus.ACTIVE);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isOwnerOrMember(UUID organizationId) {
        return isOwner(organizationId) || isMember(organizationId);
    }
}
