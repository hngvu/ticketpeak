package io.qzz.hoangvu.ticketpeak.api.offer.repository;

import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByEventIdOrderByCreatedAtAsc(UUID eventId);

    Optional<Offer> findByEventIdAndTicketTypeId(UUID eventId, String ticketTypeId);

    boolean existsByEventIdAndTicketTypeId(UUID eventId, String ticketTypeId);

    void deleteByEventIdAndTicketTypeId(UUID eventId, String ticketTypeId);
}
