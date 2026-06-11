package io.qzz.hoangvu.ticketpeak.api.account.repository;

import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT a FROM Account a
            WHERE (CAST(:q AS string) IS NULL OR
                   LOWER(a.email) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR
                   LOWER(CONCAT(COALESCE(a.firstName, ''), ' ', COALESCE(a.lastName, '')))
                       LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
              AND (:role IS NULL OR :role MEMBER OF a.roles)
            ORDER BY a.email
            """)
    List<Account> listAccounts(@Param("q") String q,
                               @Param("role") Role role,
                               Pageable pageable);
}
