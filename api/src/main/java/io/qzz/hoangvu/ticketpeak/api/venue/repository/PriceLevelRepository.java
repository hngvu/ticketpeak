package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.PriceLevel;
import io.qzz.hoangvu.ticketpeak.api.venue.model.PriceLevelId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceLevelRepository extends JpaRepository<PriceLevel, PriceLevelId> {
    List<PriceLevel> findByManifestId(String manifestId);
}
