package io.qzz.hoangvu.ticketpeak.api.resale.repository;

import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrder;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResaleOrderRepository extends JpaRepository<ResaleOrder, UUID> {
    Optional<ResaleOrder> findByResaleListingIdAndStatus(UUID resaleListingId, ResaleOrderStatus status);
}
