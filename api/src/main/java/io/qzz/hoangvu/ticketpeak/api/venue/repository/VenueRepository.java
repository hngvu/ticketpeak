package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

    @Query("""
            SELECT v FROM Venue v
            WHERE (:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:status IS NULL OR v.status = :status)
            """)
    Page<Venue> search(@Param("name") String name, @Param("status") VenueStatus status, Pageable pageable);

    @Query("""
            SELECT v FROM Venue v
            WHERE (:city IS NULL OR LOWER(v.city) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:country IS NULL OR LOWER(v.country) LIKE LOWER(CONCAT('%', :country, '%')))
            """)
    java.util.List<Venue> searchByLocation(@Param("city") String city, @Param("country") String country);

    @Query("""
            SELECT v FROM Venue v
            WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(v.city) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(v.country) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    java.util.List<Venue> searchByKeyword(@Param("query") String query);
}
