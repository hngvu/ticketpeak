package io.qzz.hoangvu.ticketpeak.api.event.repository;

import io.qzz.hoangvu.ticketpeak.api.event.model.EventAttraction;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventAttractionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventAttractionRepository extends JpaRepository<EventAttraction, EventAttractionId> {
    List<EventAttraction> findByEventId(UUID eventId);
    void deleteByEventId(UUID eventId);

    @Query("SELECT ea.eventId FROM EventAttraction ea WHERE ea.attractionId IN :attractionIds")
    List<UUID> findEventIdsByAttractionIds(@Param("attractionIds") List<UUID> attractionIds);

    List<EventAttraction> findByEventIdIn(List<UUID> eventIds);
}
