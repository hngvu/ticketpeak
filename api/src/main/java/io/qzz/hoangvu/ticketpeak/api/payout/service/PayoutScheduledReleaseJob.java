package io.qzz.hoangvu.ticketpeak.api.payout.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutScheduledReleaseJob {
    private final PayoutService payoutService;

    @Scheduled(cron = "0 0 8 * * *")
    public void releaseEligiblePayouts() {
        log.info("Starting daily scheduled release check...");
        List<UUID> eligibleIds = payoutService.getEligiblePayoutIds(Instant.now());
        int successCount = 0;
        for (UUID id : eligibleIds) {
            try {
                payoutService.releasePayout(id);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to release eligible payout ID: {}", id, e);
            }
        }
        log.info("Successfully transitioned {}/{} eligible payout(s) to PROCESSING status.", successCount, eligibleIds.size());
    }
}
