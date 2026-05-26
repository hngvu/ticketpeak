package io.qzz.hoangvu.ticketpeak.api.offer.repository;

import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    List<TicketType> findByEventIdOrderByCreatedAtAsc(UUID eventId);

    Optional<TicketType> findByEventIdAndSlug(UUID eventId, String slug);

    boolean existsByEventIdAndSlug(UUID eventId, String slug);
}
