package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Section;
import io.qzz.hoangvu.ticketpeak.api.venue.model.SectionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, SectionId> {
    List<Section> findByManifestId(String manifestId);
}
