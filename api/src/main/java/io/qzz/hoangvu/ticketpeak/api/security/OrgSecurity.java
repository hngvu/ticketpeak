package io.qzz.hoangvu.ticketpeak.api.security;

import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
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
    private final EventRepository eventRepository;
    private final io.qzz.hoangvu.ticketpeak.api.iam.repository.AccountPermissionRepository accountPermissionRepository;

    public OrgSecurity(OrganizationRepository organizationRepository,
                       OrganizationMemberRepository organizationMemberRepository,
                       EventRepository eventRepository,
                       io.qzz.hoangvu.ticketpeak.api.iam.repository.AccountPermissionRepository accountPermissionRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.eventRepository = eventRepository;
        this.accountPermissionRepository = accountPermissionRepository;
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

    public boolean isEventOwnerOrMember(UUID eventId) {
        if (eventId == null) {
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
            return eventRepository.findById(eventId)
                    .map(event -> {
                        Organization org = organizationRepository.findById(event.getOrganizationId()).orElse(null);
                        if (org == null) {
                            return false;
                        }
                        if (org.getOwnerAccountId().equals(acc.accountId())) {
                            return true;
                        }
                        return organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(
                                event.getOrganizationId(), acc.accountId(), OrganizationMemberStatus.ACTIVE);
                    })
                    .orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasPermission(UUID organizationId, String permissionCode) {
        if (organizationId == null || permissionCode == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }

        if (!(auth.getPrincipal() instanceof AuthenticatedAccount acc)) {
            return false;
        }

        if (acc.role() == io.qzz.hoangvu.ticketpeak.api.iam.model.Role.ADMIN) {
            return true;
        }

        if (isOwner(organizationId)) {
            return true;
        }

        try {
            return accountPermissionRepository.existsByAccountIdAndPermissionCodeAndOrganizationIdAndIsActiveTrue(
                    acc.accountId(), permissionCode, organizationId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
