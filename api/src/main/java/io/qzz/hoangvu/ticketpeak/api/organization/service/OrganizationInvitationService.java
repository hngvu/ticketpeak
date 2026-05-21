package io.qzz.hoangvu.ticketpeak.api.organization.service;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.CreateInvitationRequest;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.InvitationDetailsResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.dto.InvitationResponse;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitation;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationInvitationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMember;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationInvitationRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationInvitationService {

    private final OrganizationInvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AccountRepository accountRepository;

    public OrganizationInvitationService(
            OrganizationInvitationRepository invitationRepository,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            AccountRepository accountRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwner(#orgId)")
    public InvitationResponse createInvitation(UUID orgId, CreateInvitationRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));

        AuthenticatedAccount principal = (AuthenticatedAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Account invitee = accountRepository.findById(request.inviteeAccountId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Invitee account not found"));

        if (invitee.getRole() != Role.ORGANIZER && invitee.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INVITEE_ROLE", "Invitee must be an organizer");
        }

        boolean isAlreadyMember = organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(
                orgId, invitee.getId(), OrganizationMemberStatus.ACTIVE);
        if (isAlreadyMember) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_MEMBER", "Account is already an active member");
        }

        boolean hasPendingOrAccepted = invitationRepository.existsByOrganizationIdAndInviteeAccountIdAndStatusIn(
                orgId, invitee.getId(), List.of(OrganizationInvitationStatus.PENDING, OrganizationInvitationStatus.ACCEPTED));
        if (hasPendingOrAccepted) {
            throw new ApiException(HttpStatus.CONFLICT, "INVITATION_EXISTS", "A pending or accepted invitation already exists for this account");
        }

        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        OrganizationInvitation invitation = OrganizationInvitation.builder()
                .organization(org)
                .inviteeAccountId(invitee.getId())
                .invitedBy(principal.accountId())
                .token(token)
                .status(OrganizationInvitationStatus.PENDING)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        return InvitationResponse.from(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @orgSecurity.isOwnerOrMember(#orgId)")
    public List<InvitationResponse> getOrganizationInvitations(UUID orgId) {
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "Organization not found"));
                
        return invitationRepository.findByOrganizationId(orgId).stream()
                .map(InvitationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getMyInvitations() {
        AuthenticatedAccount principal = (AuthenticatedAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return invitationRepository.findByInviteeAccountId(principal.accountId()).stream()
                .map(InvitationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvitationDetailsResponse validateToken(String token) {
        OrganizationInvitation invitation = getAndCheckExpiration(token);

        String inviterName = accountRepository.findById(invitation.getInvitedBy())
                .map(a -> {
                    String name = "";
                    if (a.getFirstName() != null) name += a.getFirstName();
                    if (a.getLastName() != null) name += (name.isEmpty() ? "" : " ") + a.getLastName();
                    return name.isEmpty() ? a.getEmail() : name;
                })
                .orElse("Unknown");

        return new InvitationDetailsResponse(
                invitation.getOrganization().getName(),
                inviterName,
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }

    @Transactional
    public void acceptInvitation(String token) {
        AuthenticatedAccount principal = (AuthenticatedAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OrganizationInvitation invitation = getAndCheckExpiration(token);

        if (invitation.getStatus() != OrganizationInvitationStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Invitation is not pending");
        }

        if (!invitation.getInviteeAccountId().equals(principal.accountId())) {
            throw new AccessDeniedException("This invitation is not for your account");
        }

        invitation.setStatus(OrganizationInvitationStatus.ACCEPTED);
        invitation.setRespondedAt(Instant.now());
        invitationRepository.save(invitation);

        OrganizationMember member = organizationMemberRepository
                .findByOrganizationIdAndAccountId(invitation.getOrganization().getId(), principal.accountId())
                .orElseGet(() -> OrganizationMember.builder()
                        .organization(invitation.getOrganization())
                        .accountId(principal.accountId())
                        .joinedAt(Instant.now())
                        .build());

        member.setStatus(OrganizationMemberStatus.ACTIVE);
        organizationMemberRepository.save(member);
    }

    @Transactional
    public void rejectInvitation(String token) {
        AuthenticatedAccount principal = (AuthenticatedAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OrganizationInvitation invitation = getAndCheckExpiration(token);

        if (invitation.getStatus() != OrganizationInvitationStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Invitation is not pending");
        }

        if (!invitation.getInviteeAccountId().equals(principal.accountId())) {
            throw new AccessDeniedException("This invitation is not for your account");
        }

        invitation.setStatus(OrganizationInvitationStatus.REJECTED);
        invitation.setRespondedAt(Instant.now());
        invitationRepository.save(invitation);
    }

    private OrganizationInvitation getAndCheckExpiration(String token) {
        OrganizationInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVITATION_NOT_FOUND", "Invitation not found"));

        if (invitation.getStatus() == OrganizationInvitationStatus.PENDING && invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(OrganizationInvitationStatus.EXPIRED);
            invitation = invitationRepository.save(invitation);
        }

        return invitation;
    }
}
