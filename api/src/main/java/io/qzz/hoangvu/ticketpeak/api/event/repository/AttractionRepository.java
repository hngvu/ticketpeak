package io.qzz.hoangvu.ticketpeak.api.event.repository;

import io.qzz.hoangvu.ticketpeak.api.event.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, UUID> {
    Optional<Attraction> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT a FROM Attraction a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Attraction> searchByNameOrDescription(@Param("query") String query);
}
