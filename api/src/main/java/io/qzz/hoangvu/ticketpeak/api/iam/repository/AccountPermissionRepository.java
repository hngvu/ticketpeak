package io.qzz.hoangvu.ticketpeak.api.iam.repository;

import io.qzz.hoangvu.ticketpeak.api.iam.model.AccountPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountPermissionRepository extends JpaRepository<AccountPermission, Long> {

    Optional<AccountPermission> findByAccountIdAndPermissionCodeAndOrganizationId(UUID accountId, String permissionCode, UUID organizationId);

    List<AccountPermission> findByAccountIdAndOrganizationIdAndIsActiveTrue(UUID accountId, UUID organizationId);

    boolean existsByAccountIdAndPermissionCodeAndOrganizationIdAndIsActiveTrue(UUID accountId, String permissionCode, UUID organizationId);
}
