package io.qzz.hoangvu.ticketpeak.api.venue.repository;

import io.qzz.hoangvu.ticketpeak.api.venue.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, String> {
    List<Seat> findBySeatRowId(String rowId);
    boolean existsBySeatRowIdAndName(String rowId, String name);
    long countBySeatRowRsAreaId(String rsAreaId);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Seat s WHERE s.seatRow.rsArea.manifestId = :manifestId")
    List<Seat> findByManifestId(@org.springframework.data.repository.query.Param("manifestId") String manifestId);
}
