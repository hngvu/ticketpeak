package io.qzz.hoangvu.ticketpeak.api.event.repository;

import io.qzz.hoangvu.ticketpeak.api.event.model.EventClassification;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventClassificationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventClassificationRepository extends JpaRepository<EventClassification, EventClassificationId> {
    List<EventClassification> findByEventId(UUID eventId);
    void deleteByEventId(UUID eventId);

    @Query("SELECT ec.eventId FROM EventClassification ec WHERE ec.classificationId IN :classificationIds")
    List<UUID> findEventIdsByClassificationIds(@Param("classificationIds") List<UUID> classificationIds);

    List<EventClassification> findByEventIdIn(List<UUID> eventIds);
}
