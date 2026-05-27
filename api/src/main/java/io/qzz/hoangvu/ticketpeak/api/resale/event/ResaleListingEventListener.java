package io.qzz.hoangvu.ticketpeak.api.resale.event;

import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleListingRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.event.TicketsVoidedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResaleListingEventListener {

    private final ResaleListingRepository resaleListingRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTicketsVoided(TicketsVoidedEvent event) {
        for (UUID ticketId : event.ticketIds()) {
            resaleListingRepository.findActiveByTicketId(ticketId,
                    List.of(ResaleListingStatus.ACTIVE, ResaleListingStatus.RESERVED))
                    .ifPresent(listing -> {
                        listing.setStatus(ResaleListingStatus.CANCELLED);
                        resaleListingRepository.save(listing);
                    });
        }
    }
}
