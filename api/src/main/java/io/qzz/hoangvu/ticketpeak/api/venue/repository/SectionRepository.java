package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Section;
import io.qzz.hoangvu.ticketpeak.api.venue.model.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, String> {
    List<Section> findByManifestId(String manifestId);
    List<Section> findByManifestIdAndType(String manifestId, SectionType type);
}
