package io.qzz.hoangvu.ticketpeak.api.offer.repository;

import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferSaleWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OfferSaleWindowRepository extends JpaRepository<OfferSaleWindow, UUID> {
}
