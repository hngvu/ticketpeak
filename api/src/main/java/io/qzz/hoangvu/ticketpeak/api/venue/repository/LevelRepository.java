package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Level;
import io.qzz.hoangvu.ticketpeak.api.venue.model.LevelId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level, LevelId> {
    List<Level> findByManifestId(String manifestId);
}
