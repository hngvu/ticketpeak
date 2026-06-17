package io.qzz.hoangvu.ticketpeak.api.offer.repository;

import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByEventIdOrderByCreatedAtAsc(UUID eventId);
    List<Offer> findByEventId(UUID eventId);

    Optional<Offer> findByEventIdAndTicketTypeId(UUID eventId, UUID ticketTypeId);

    boolean existsByEventIdAndTicketTypeId(UUID eventId, UUID ticketTypeId);
    
    boolean existsByEventIdAndCode(UUID eventId, String code);

    void deleteByEventIdAndTicketTypeId(UUID eventId, UUID ticketTypeId);
    
    Optional<Offer> findByEventIdAndId(UUID eventId, UUID id);
    
    void deleteByEventIdAndId(UUID eventId, UUID id);
}
