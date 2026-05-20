package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Manifest;
import io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManifestRepository extends JpaRepository<Manifest, String> {

    List<Manifest> findByVenueId(UUID venueId);

    boolean existsByVenueIdAndStatus(UUID venueId, ManifestStatus status);

    Optional<Manifest> findByVenueIdAndStatus(UUID venueId, ManifestStatus status);
}
