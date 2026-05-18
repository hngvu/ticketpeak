package io.qzz.hoangvu.ticketpeak.api.organization.service;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.AccountPermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.service.PermissionConstants;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.MemberAccountSummary;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationMemberResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationMemberService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AccountRepository accountRepository;
    private final AccountPermissionRepository accountPermissionRepository;

    public OrganizationMemberService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            AccountRepository accountRepository,
            AccountPermissionRepository accountPermissionRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.accountRepository = accountRepository;
        this.accountPermissionRepository = accountPermissionRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> getOrganizationMembers(UUID orgId, OrganizationMemberStatus status, AuthenticatedAccount principal) {
        Organization org = getOrganizationAndVerifyReadAccess(orgId, principal);

        List<OrganizationMember> members = (status != null)
                ? organizationMemberRepository.findByOrganizationIdAndStatus(orgId, status)
                : organizationMemberRepository.findByOrganizationId(orgId);

        List<UUID> accountIds = members.stream()
                .map(OrganizationMember::getAccountId)
                .distinct()
                .toList();

        Map<UUID, Account> accountMap = accountRepository.findAllById(accountIds)
                .stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        return members.stream()
                .map(m -> OrganizationMemberResponse.from(m, MemberAccountSummary.from(accountMap.get(m.getAccountId()))))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationMemberResponse getMemberStatus(UUID orgId, UUID accountId, AuthenticatedAccount principal) {
        getOrganizationAndVerifyReadAccess(orgId, principal);

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Organization member not found"));

        Account account = accountRepository.findById(accountId).orElse(null);

        return OrganizationMemberResponse.from(member, MemberAccountSummary.from(account));
    }

    @Transactional
    public void removeMember(UUID orgId, UUID accountId, AuthenticatedAccount principal) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        if (principal.role() != Role.ADMIN && !org.getOwnerAccountId().equals(principal.accountId())) {
            boolean hasRemovePerm = accountPermissionRepository.existsByAccountIdAndPermissionCodeAndOrganizationIdAndIsActiveTrue(
                    principal.accountId(), PermissionConstants.ORG_MEMBER_REMOVE, orgId);
            if (!hasRemovePerm) {
                throw new AccessDeniedException("You do not have permission to remove members from this organization");
            }
        }

        if (org.getOwnerAccountId().equals(accountId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_REMOVE_OWNER", "Cannot remove the organization owner");
        }

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Organization member not found"));

        if (member.getStatus() == OrganizationMemberStatus.REMOVED || member.getStatus() == OrganizationMemberStatus.LEFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", "Member is already inactive");
        }

        member.setStatus(OrganizationMemberStatus.REMOVED);
        member.setUpdatedAt(Instant.now());
        organizationMemberRepository.save(member);
    }

    @Transactional
    public void leaveOrganization(UUID orgId, AuthenticatedAccount principal) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        if (org.getOwnerAccountId().equals(principal.accountId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_LEAVE_AS_OWNER", "Organization owner cannot leave the organization");
        }

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, principal.accountId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Organization member not found"));

        if (member.getStatus() == OrganizationMemberStatus.REMOVED || member.getStatus() == OrganizationMemberStatus.LEFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", "You are already not an active member");
        }

        member.setStatus(OrganizationMemberStatus.LEFT);
        member.setUpdatedAt(Instant.now());
        organizationMemberRepository.save(member);
    }

    @Transactional
    public OrganizationMemberResponse restoreMember(UUID orgId, UUID accountId, AuthenticatedAccount principal) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        if (principal.role() != Role.ADMIN && !org.getOwnerAccountId().equals(principal.accountId())) {
            boolean hasInvitePerm = accountPermissionRepository.existsByAccountIdAndPermissionCodeAndOrganizationIdAndIsActiveTrue(
                    principal.accountId(), PermissionConstants.ORG_MEMBER_INVITE, orgId);
            if (!hasInvitePerm) {
                throw new AccessDeniedException("You do not have permission to restore members for this organization");
            }
        }

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Organization member not found"));

        if (member.getStatus() == OrganizationMemberStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", "Member is already active");
        }

        member.setStatus(OrganizationMemberStatus.ACTIVE);
        member.setUpdatedAt(Instant.now());
        OrganizationMember savedMember = organizationMemberRepository.save(member);

        Account account = accountRepository.findById(accountId).orElse(null);
        return OrganizationMemberResponse.from(savedMember, MemberAccountSummary.from(account));
    }

    private Organization getOrganizationAndVerifyReadAccess(UUID orgId, AuthenticatedAccount principal) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        if (principal.role() == Role.ADMIN || org.getOwnerAccountId().equals(principal.accountId())) {
            return org;
        }

        boolean isMember = organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(
                orgId, principal.accountId(), OrganizationMemberStatus.ACTIVE);

        if (!isMember) {
            throw new AccessDeniedException("You are not an active member of this organization");
        }

        return org;
    }
}
