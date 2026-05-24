package io.qzz.hoangvu.ticketpeak.api.event.repository;

import io.qzz.hoangvu.ticketpeak.api.event.model.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ClassificationRepository extends JpaRepository<Classification, UUID> {
    Optional<Classification> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Classification> findByParentId(UUID parentId);

    @Query("SELECT c FROM Classification c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Classification> searchByName(@Param("query") String query);

    @Query(value = "WITH RECURSIVE sub_classifications AS (" +
                   "  SELECT id FROM classification WHERE id = :classificationId " +
                   "  UNION ALL " +
                   "  SELECT c.id FROM classification c " +
                   "  INNER JOIN sub_classifications s ON c.parent_id = s.id" +
                   ") SELECT id FROM sub_classifications", nativeQuery = true)
    List<UUID> findDescendantIds(@Param("classificationId") UUID classificationId);
}
