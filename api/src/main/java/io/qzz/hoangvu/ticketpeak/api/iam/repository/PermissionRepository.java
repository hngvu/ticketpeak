package io.qzz.hoangvu.ticketpeak.api.iam.repository;

import io.qzz.hoangvu.ticketpeak.api.iam.model.Permission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    List<Permission> findByScope(PermissionScope scope);
}
