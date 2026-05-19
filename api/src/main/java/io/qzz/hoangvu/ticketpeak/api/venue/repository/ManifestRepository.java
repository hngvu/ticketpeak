package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Manifest;
import io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManifestRepository extends JpaRepository<Manifest, String> {

    List<Manifest> findByVenueId(String venueId);

    boolean existsByVenueIdAndStatus(String venueId, ManifestStatus status);

    Optional<Manifest> findByVenueIdAndStatus(String venueId, ManifestStatus status);
}
