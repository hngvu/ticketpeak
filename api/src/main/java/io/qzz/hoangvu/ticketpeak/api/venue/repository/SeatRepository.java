package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, String> {
    List<Seat> findBySeatRowId(String rowId);
    boolean existsBySeatRowIdAndName(String rowId, String name);
    long countBySeatRowRsAreaId(String rsAreaId);
}
