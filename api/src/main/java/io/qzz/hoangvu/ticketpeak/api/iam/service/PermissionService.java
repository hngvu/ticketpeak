package io.qzz.hoangvu.ticketpeak.api.iam.service;

import io.qzz.hoangvu.ticketpeak.api.iam.dto.AccountPermissionResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.CreatePermissionRequest;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.GrantPermissionRequest;
import io.qzz.hoangvu.ticketpeak.api.iam.dto.PermissionResponse;
import io.qzz.hoangvu.ticketpeak.api.iam.exception.IamException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.AccountPermission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Permission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.AccountPermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.repository.PermissionRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.exception.OrganizationException;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationMemberStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final AccountPermissionRepository accountPermissionRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public PermissionService(
            PermissionRepository permissionRepository,
            AccountPermissionRepository accountPermissionRepository,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.accountPermissionRepository = accountPermissionRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request, AuthenticatedAccount principal) {
        if (principal.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only platform admins can create permissions");
        }
        if (permissionRepository.existsById(request.code())) {
            throw IamException.permissionAlreadyExists();
        }
        Permission permission = Permission.builder()
                .code(request.code())
                .name(request.name())
                .scope(request.scope())
                .action(request.action())
                .resource(request.resource())
                .build();
        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions(PermissionScope scope) {
        List<Permission> permissions = (scope != null)
                ? permissionRepository.findByScope(scope)
                : permissionRepository.findAll();
        return permissions.stream().map(PermissionResponse::from).toList();
    }

    @Transactional
    public AccountPermissionResponse grantPermission(UUID orgId, GrantPermissionRequest request, AuthenticatedAccount principal) {
        verifyOrgAdminOrManager(orgId, principal);
        verifyActiveOrgMemberOrOwner(orgId, request.accountId());

        Permission permission = permissionRepository.findById(request.permissionCode())
                .orElseThrow(IamException::permissionNotFound);

        if (permission.getScope() != PermissionScope.ORGANIZATION) {
            throw IamException.invalidPermissionScope();
        }

        return accountPermissionRepository.findByAccountIdAndPermissionCodeAndOrganizationId(request.accountId(), request.permissionCode(), orgId)
                .map(existing -> {
                    if (Boolean.TRUE.equals(existing.getIsActive())) {
                        throw IamException.permissionAlreadyGranted();
                    }
                    existing.setIsActive(true);
                    existing.setGrantedAt(Instant.now());
                    existing.setGrantedBy(principal.accountId());
                    existing.setRevokedBy(null);
                    existing.setUpdatedAt(Instant.now());
                    return AccountPermissionResponse.from(accountPermissionRepository.save(existing));
                })
                .orElseGet(() -> {
                    AccountPermission newGrant = AccountPermission.builder()
                            .accountId(request.accountId())
                            .organizationId(orgId)
                            .permission(permission)
                            .isActive(true)
                            .grantedAt(Instant.now())
                            .grantedBy(principal.accountId())
                            .updatedAt(Instant.now())
                            .build();
                    return AccountPermissionResponse.from(accountPermissionRepository.save(newGrant));
                });
    }

    @Transactional
    public void revokePermission(UUID orgId, UUID accountId, String permissionCode, AuthenticatedAccount principal) {
        verifyOrgAdminOrManager(orgId, principal);

        AccountPermission grant = accountPermissionRepository.findByAccountIdAndPermissionCodeAndOrganizationId(accountId, permissionCode, orgId)
                .filter(ap -> Boolean.TRUE.equals(ap.getIsActive()))
                .orElseThrow(IamException::permissionGrantNotFound);

        grant.setIsActive(false);
        grant.setRevokedBy(principal.accountId());
        grant.setUpdatedAt(Instant.now());
        accountPermissionRepository.save(grant);
    }

    @Transactional(readOnly = true)
    public List<AccountPermissionResponse> getAccountPermissions(UUID orgId, UUID accountId, AuthenticatedAccount principal) {
        // Verify org exists
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(OrganizationException::notFound);

        boolean isSelf = principal.accountId().equals(accountId);
        boolean isAdmin = principal.role() == Role.ADMIN;
        boolean isOwner = org.getOwnerAccountId().equals(principal.accountId());
        boolean isMember = organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(orgId, principal.accountId(), OrganizationMemberStatus.ACTIVE);

        if (!isSelf && !isAdmin && !isOwner && !isMember) {
            throw new AccessDeniedException("You are not authorized to view permissions in this organization");
        }

        return accountPermissionRepository.findByAccountIdAndOrganizationIdAndIsActiveTrue(accountId, orgId)
                .stream()
                .map(AccountPermissionResponse::from)
                .toList();
    }

    private void verifyOrgAdminOrManager(UUID orgId, AuthenticatedAccount principal) {
        if (principal.role() == Role.ADMIN) {
            return;
        }
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(OrganizationException::notFound);

        if (org.getOwnerAccountId().equals(principal.accountId())) {
            return;
        }

        boolean isMember = organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(orgId, principal.accountId(), OrganizationMemberStatus.ACTIVE);
        if (!isMember) {
            throw new AccessDeniedException("You are not an active member of this organization");
        }

        boolean hasManagePerm = accountPermissionRepository.existsByAccountIdAndPermissionCodeAndOrganizationIdAndIsActiveTrue(
                principal.accountId(),
                PermissionConstants.ORG_MANAGE_PERMISSIONS,
                orgId
        );
        if (!hasManagePerm) {
            throw new AccessDeniedException("You do not have permission to manage organization permissions");
        }
    }

    private void verifyActiveOrgMemberOrOwner(UUID orgId, UUID targetAccountId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(OrganizationException::notFound);

        if (org.getOwnerAccountId().equals(targetAccountId)) {
            return;
        }

        boolean isMember = organizationMemberRepository.existsByOrganizationIdAndAccountIdAndStatus(orgId, targetAccountId, OrganizationMemberStatus.ACTIVE);
        if (!isMember) {
            throw OrganizationException.notOrganizationMember();
        }
    }
}
