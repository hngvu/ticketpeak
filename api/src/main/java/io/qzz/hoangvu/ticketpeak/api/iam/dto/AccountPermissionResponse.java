package io.qzz.hoangvu.ticketpeak.api.iam.dto;

import io.qzz.hoangvu.ticketpeak.api.iam.model.AccountPermission;

import java.time.Instant;
import java.util.UUID;

public record AccountPermissionResponse(
        Long id,
        UUID accountId,
        UUID organizationId,
        PermissionResponse permission,
        Boolean isActive,
        Instant grantedAt,
        UUID grantedBy,
        Instant updatedAt,
        UUID revokedBy
) {

    public static AccountPermissionResponse from(AccountPermission accountPermission) {
        return new AccountPermissionResponse(
                accountPermission.getId(),
                accountPermission.getAccountId(),
                accountPermission.getOrganizationId(),
                PermissionResponse.from(accountPermission.getPermission()),
                accountPermission.getIsActive(),
                accountPermission.getGrantedAt(),
                accountPermission.getGrantedBy(),
                accountPermission.getUpdatedAt(),
                accountPermission.getRevokedBy()
        );
    }
}
