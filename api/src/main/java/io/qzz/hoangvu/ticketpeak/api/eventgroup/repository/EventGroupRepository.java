package io.qzz.hoangvu.ticketpeak.api.eventgroup.repository;

import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface EventGroupRepository extends JpaRepository<EventGroup, UUID> {
    Optional<EventGroup> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT eg FROM EventGroup eg WHERE " +
           "(:organizationId IS NULL OR eg.organizationId = :organizationId) AND " +
           "(:query IS NULL OR LOWER(eg.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<EventGroup> searchGroups(
            @Param("query") String query,
            @Param("organizationId") UUID organizationId,
            Pageable pageable
    );
}
