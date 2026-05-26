package io.qzz.hoangvu.ticketpeak.api.payout;

import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.CreatePayoutMethodRequest;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutMethodResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.PayoutResponse;
import io.qzz.hoangvu.ticketpeak.api.payout.dto.UpdatePayoutStatusRequest;
import io.qzz.hoangvu.ticketpeak.api.payout.model.*;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutMethodRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutMethodService;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutScheduledReleaseJob;
import io.qzz.hoangvu.ticketpeak.api.payout.service.PayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class PayoutServiceIT {

    @Autowired PayoutRepository payoutRepository;
    @Autowired PayoutMethodRepository payoutMethodRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PayoutMethodService payoutMethodService;
    @Autowired PayoutService payoutService;
    @Autowired PayoutScheduledReleaseJob releaseJob;

    Account seller;

    @BeforeEach
    void setup() {
        payoutRepository.deleteAll();
        payoutMethodRepository.deleteAll();
        accountRepository.deleteAll();

        seller = accountRepository.save(Account.builder()
                .email(UUID.randomUUID() + "@example.com")
                .password("hash")
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());
    }

    @Test
    void addPayoutMethod_successAndUnsetOldPrimary() {
        CreatePayoutMethodRequest firstReq = new CreatePayoutMethodRequest(
                PayoutMethodType.BANK_TRANSFER, "Sellers Name", "VCB", "1234567890", true
        );
        PayoutMethodResponse firstRes = payoutMethodService.addMethod(seller.getId(), firstReq);
        assertThat(firstRes.isPrimary()).isTrue();
        assertThat(firstRes.maskedAccountNumber()).isEqualTo("****7890");

        CreatePayoutMethodRequest secondReq = new CreatePayoutMethodRequest(
                PayoutMethodType.MOMO, "Sellers Name", null, "0987654321", true
        );
        PayoutMethodResponse secondRes = payoutMethodService.addMethod(seller.getId(), secondReq);
        assertThat(secondRes.isPrimary()).isTrue();
        assertThat(secondRes.maskedAccountNumber()).isEqualTo("****4321");

        // Verify old primary has been unset to false
        List<PayoutMethodResponse> activeMethods = payoutMethodService.listMethods(seller.getId());
        assertThat(activeMethods).hasSize(2);
        PayoutMethodResponse oldPrimary = activeMethods.stream()
                .filter(m -> m.id().equals(firstRes.id()))
                .findFirst()
                .orElseThrow();
        assertThat(oldPrimary.isPrimary()).isFalse();
    }

    @Test
    @Transactional
    void dbUniqueConstraint_guardsAgainstMultiplePrimaries() {
        // Create first primary directly via repository to bypass service check
        PayoutMethod first = PayoutMethod.builder()
                .accountId(seller.getId())
                .type(PayoutMethodType.BANK_TRANSFER)
                .isPrimary(true)
                .holderName("Holder")
                .accountNumberEnc("someEncryptedString")
                .status(PayoutMethodStatus.ACTIVE)
                .build();
        payoutMethodRepository.save(first);

        // Try to save second primary direct to DB which should trigger unique index violation
        PayoutMethod second = PayoutMethod.builder()
                .accountId(seller.getId())
                .type(PayoutMethodType.MOMO)
                .isPrimary(true)
                .holderName("Holder")
                .accountNumberEnc("anotherEncryptedString")
                .status(PayoutMethodStatus.ACTIVE)
                .build();

        assertThatThrownBy(() -> payoutMethodRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void createPayout_successPlatformFeeCalculationsAndSnapshot() {
        CreatePayoutMethodRequest req = new CreatePayoutMethodRequest(
                PayoutMethodType.BANK_TRANSFER, "Sellers Name", "VCB", "1234567890", true
        );
        PayoutMethodResponse pm = payoutMethodService.addMethod(seller.getId(), req);

        UUID resaleListingId = UUID.randomUUID();
        BigDecimal grossAmount = BigDecimal.valueOf(1000000); // 1,000,000 VND
        BigDecimal feePercent = BigDecimal.valueOf(5.5); // 5.5%

        Instant endAt = Instant.now().plus(2, ChronoUnit.DAYS);
        PayoutResponse payout = payoutService.createPayout(
                resaleListingId, seller.getId(), grossAmount, feePercent, "VND", endAt, null
        );

        assertThat(payout.status()).isEqualTo(PayoutStatus.PENDING);
        assertThat(payout.grossAmount()).isEqualByComparingTo(grossAmount);
        assertThat(payout.platformFeePercent()).isEqualByComparingTo(feePercent);
        // platformFeeAmount = 1,000,000 * 5.5 / 100 = 55,000
        assertThat(payout.platformFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(55000));
        // netAmount = 1,000,000 - 55,000 = 945,000
        assertThat(payout.netAmount()).isEqualByComparingTo(BigDecimal.valueOf(945000));

        // Verify snapshot values
        assertThat(payout.payoutMethodSnapshot().payoutMethodId()).isEqualTo(pm.id());
        assertThat(payout.payoutMethodSnapshot().type()).isEqualTo(PayoutMethodType.BANK_TRANSFER);
        assertThat(payout.payoutMethodSnapshot().holderName()).isEqualTo("Sellers Name");
        assertThat(payout.payoutMethodSnapshot().bankCode()).isEqualTo("VCB");
        assertThat(payout.payoutMethodSnapshot().maskedAccountNumber()).isEqualTo("****7890");
    }

    @Test
    void deletePayoutMethod_softDeleteAndGuardReferenced() {
        CreatePayoutMethodRequest req = new CreatePayoutMethodRequest(
                PayoutMethodType.BANK_TRANSFER, "Sellers Name", "VCB", "1234567890", true
        );
        PayoutMethodResponse pm = payoutMethodService.addMethod(seller.getId(), req);

        // Reference it in a pending payout
        BigDecimal grossAmount = BigDecimal.valueOf(500000);
        BigDecimal feePercent = BigDecimal.valueOf(5);
        payoutService.createPayout(UUID.randomUUID(), seller.getId(), grossAmount, feePercent, "VND", Instant.now(), null);

        // Delete should be blocked
        assertThatThrownBy(() -> payoutMethodService.removeMethod(seller.getId(), pm.id()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cannot delete payout method because it is referenced by active payouts");

        // Complete the payout (mark paid) to allow deletion
        List<UUID> eligibleIds = payoutService.getEligiblePayoutIds(Instant.now().plus(4, ChronoUnit.DAYS));
        assertThat(eligibleIds).hasSize(1);
        UUID payoutId = eligibleIds.get(0);
        
        payoutService.releasePayout(payoutId); // Transition PENDING -> PROCESSING
        payoutService.updatePayoutStatus(payoutId, new UpdatePayoutStatusRequest(PayoutStatus.PAID, "REF123", "Sent money"));

        // Now deletion should succeed
        payoutMethodService.removeMethod(seller.getId(), pm.id());
        List<PayoutMethodResponse> activeMethods = payoutMethodService.listMethods(seller.getId());
        assertThat(activeMethods).isEmpty();
    }

    @Test
    void releaseEligiblePayouts_schedulerJobIsolateTransactions() {
        CreatePayoutMethodRequest req = new CreatePayoutMethodRequest(
                PayoutMethodType.BANK_TRANSFER, "Sellers Name", "VCB", "1234567890", true
        );
        PayoutMethodResponse pm = payoutMethodService.addMethod(seller.getId(), req);

        // Payout 1: Scheduled in the past (eligible)
        PayoutResponse payout1 = payoutService.createPayout(
                UUID.randomUUID(), seller.getId(), BigDecimal.valueOf(100000), BigDecimal.valueOf(5), "VND", 
                Instant.now().minus(5, ChronoUnit.DAYS), null
        );

        // Payout 2: Scheduled in the future (not eligible)
        PayoutResponse payout2 = payoutService.createPayout(
                UUID.randomUUID(), seller.getId(), BigDecimal.valueOf(200000), BigDecimal.valueOf(5), "VND", 
                Instant.now().plus(5, ChronoUnit.DAYS), null
        );

        // Run the scheduled job
        releaseJob.releaseEligiblePayouts();

        // Payout 1 should be PROCESSING
        PayoutResponse verifyPayout1 = payoutService.getPayoutForAdmin(payout1.id());
        assertThat(verifyPayout1.status()).isEqualTo(PayoutStatus.PROCESSING);

        // Payout 2 should remain PENDING
        PayoutResponse verifyPayout2 = payoutService.getPayoutForAdmin(payout2.id());
        assertThat(verifyPayout2.status()).isEqualTo(PayoutStatus.PENDING);
    }

    @Test
    void updatePayoutStatus_strictStateTransitions() {
        payoutMethodService.addMethod(seller.getId(), new CreatePayoutMethodRequest(
                PayoutMethodType.BANK_TRANSFER, "Sellers Name", "VCB", "1234567890", true
        ));

        PayoutResponse payout = payoutService.createPayout(
                UUID.randomUUID(), seller.getId(), BigDecimal.valueOf(100000), BigDecimal.valueOf(5), "VND", 
                Instant.now().minus(5, ChronoUnit.DAYS), null
        );

        // PENDING -> PAID is invalid (skips PROCESSING)
        assertThatThrownBy(() -> payoutService.updatePayoutStatus(payout.id(), new UpdatePayoutStatusRequest(PayoutStatus.PAID, null, null)))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", "PAYOUT_INVALID_STATUS_TRANSITION");

        // PENDING -> CANCELLED is valid
        PayoutResponse cancelled = payoutService.updatePayoutStatus(payout.id(), new UpdatePayoutStatusRequest(PayoutStatus.CANCELLED, null, null));
        assertThat(cancelled.status()).isEqualTo(PayoutStatus.CANCELLED);
    }

    @Test
    void cancelPayout_customExceptionWhenNotPending() {
        payoutMethodService.addMethod(seller.getId(), new CreatePayoutMethodRequest(
                PayoutMethodType.BANK_TRANSFER, "Sellers Name", "VCB", "1234567890", true
        ));

        UUID resaleListingId = UUID.randomUUID();
        PayoutResponse payout = payoutService.createPayout(
                resaleListingId, seller.getId(), BigDecimal.valueOf(100000), BigDecimal.valueOf(5), "VND", 
                Instant.now().minus(5, ChronoUnit.DAYS), null
        );

        // Transition payout to PROCESSING
        payoutService.releasePayout(payout.id());

        // cancelPayout should throw specific exception now
        assertThatThrownBy(() -> payoutService.cancelPayout(resaleListingId))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", "PAYOUT_CANNOT_BE_CANCELLED")
                .hasMessageContaining("Only PENDING payouts can be cancelled");
    }
}
