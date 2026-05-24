package io.qzz.hoangvu.ticketpeak.api.organization.service;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.MemberAccountSummary;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.OrganizationMemberResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.exception.OrganizationException;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public OrganizationMemberService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            AccountRepository accountRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwnerOrMember(#orgId)")
    public List<OrganizationMemberResponse> getOrganizationMembers(UUID orgId, OrganizationMemberStatus status) {
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
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwnerOrMember(#orgId)")
    public OrganizationMemberResponse getMemberStatus(UUID orgId, UUID accountId) {
        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, accountId)
                .orElseThrow(OrganizationException::memberNotFound);

        Account account = accountRepository.findById(accountId).orElse(null);

        return OrganizationMemberResponse.from(member, MemberAccountSummary.from(account));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwner(#orgId) or @orgSecurity.hasPermission(#orgId, 'ORG_MEMBER:REMOVE')")
    public void removeMember(UUID orgId, UUID accountId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(OrganizationException::notFound);

        if (org.getOwnerAccountId().equals(accountId)) {
            throw OrganizationException.cannotRemoveOwner();
        }

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, accountId)
                .orElseThrow(OrganizationException::memberNotFound);

        if (member.getStatus() == OrganizationMemberStatus.REMOVED || member.getStatus() == OrganizationMemberStatus.LEFT) {
            throw OrganizationException.invalidStatusTransition("Member is already inactive");
        }

        member.setStatus(OrganizationMemberStatus.REMOVED);
        member.setUpdatedAt(Instant.now());
        organizationMemberRepository.save(member);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwnerOrMember(#orgId)")
    public void leaveOrganization(UUID orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(OrganizationException::notFound);

        AuthenticatedAccount principal = (AuthenticatedAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (org.getOwnerAccountId().equals(principal.accountId())) {
            throw OrganizationException.cannotLeaveAsOwner();
        }

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, principal.accountId())
                .orElseThrow(OrganizationException::memberNotFound);

        if (member.getStatus() == OrganizationMemberStatus.REMOVED || member.getStatus() == OrganizationMemberStatus.LEFT) {
            throw OrganizationException.invalidStatusTransition("You are already not an active member");
        }

        member.setStatus(OrganizationMemberStatus.LEFT);
        member.setUpdatedAt(Instant.now());
        organizationMemberRepository.save(member);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwner(#orgId)")
    public OrganizationMemberResponse restoreMember(UUID orgId, UUID accountId) {
        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndAccountId(orgId, accountId)
                .orElseThrow(OrganizationException::memberNotFound);

        if (member.getStatus() == OrganizationMemberStatus.ACTIVE) {
            throw OrganizationException.invalidStatusTransition("Member is already active");
        }

        member.setStatus(OrganizationMemberStatus.ACTIVE);
        member.setUpdatedAt(Instant.now());
        OrganizationMember savedMember = organizationMemberRepository.save(member);

        Account account = accountRepository.findById(accountId).orElse(null);
        return OrganizationMemberResponse.from(savedMember, MemberAccountSummary.from(account));
    }
}
