package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.RSArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RSAreaRepository extends JpaRepository<RSArea, String> {
    List<RSArea> findByManifestId(String manifestId);
}
