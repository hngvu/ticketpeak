package io.qzz.hoangvu.ticketpeak.api.payout.repository;

import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethod;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutMethodRepository extends JpaRepository<PayoutMethod, UUID> {
    List<PayoutMethod> findByAccountIdAndStatus(UUID accountId, PayoutMethodStatus status);
    Optional<PayoutMethod> findByAccountIdAndIsPrimaryTrueAndStatus(UUID accountId, PayoutMethodStatus status);
    Optional<PayoutMethod> findByIdAndAccountId(UUID id, UUID accountId);
}
