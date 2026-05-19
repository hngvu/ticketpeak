package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.SeatRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRowRepository extends JpaRepository<SeatRow, String> {
    List<SeatRow> findByRsAreaId(String rsAreaId);
    boolean existsByRsAreaIdAndName(String rsAreaId, String name);
}
