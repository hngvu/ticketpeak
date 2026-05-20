package io.qzz.hoangvu.ticketpeak.api.event.repository;

import io.qzz.hoangvu.ticketpeak.api.event.model.EventManifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventManifestRepository extends JpaRepository<EventManifest, UUID> {
}
