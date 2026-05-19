package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.GAArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GAAreaRepository extends JpaRepository<GAArea, String> {
    List<GAArea> findByManifestId(String manifestId);
}
