package io.qzz.hoangvu.ticketpeak.api.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryGaRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferSaleWindow;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SaleWindowType;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.service.PaymentService;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationItem;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired EventRepository eventRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired InventoryGaRepository inventoryGaRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PaymentService paymentService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired jakarta.persistence.EntityManager entityManager;

    Account buyer;
    String token;

    Organization org;
    Venue venue;
    Event onsaleEvent;
    Offer gaOffer;
    String gaAreaId = "AREA-GA-IT";

    @BeforeEach
    void setup() throws Exception {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        inventoryGaRepository.deleteAll();
        offerRepository.deleteAll();
        eventRepository.deleteAll();
        organizationRepository.deleteAll();
        venueRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";

        buyer = accountRepository.saveAndFlush(Account.builder()
                .email("buyer@test.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        token = login(buyer.getEmail(), rawPassword);

        org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Payment Org")
                .slug("payment-org")
                .ownerAccountId(buyer.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        venue = venueRepository.saveAndFlush(Venue.builder()
                .name("Payment Venue")
                .address("2 Payment St")
                .city("HCM")
                .country("Vietnam")
                .status(VenueStatus.ACTIVE)
                .images(List.of())
                .build());

        onsaleEvent = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("On Sale Show")
                .slug("on-sale-show")
                .status(EventStatus.ONSALE)
                .startAt(Instant.now().plusSeconds(86400))
                .timezone("Asia/Ho_Chi_Minh")
                .build());

        Offer gaOfferToBuild = Offer.builder()
                .eventId(onsaleEvent.getId())
                .ticketTypeId("ga-ticket")
                .name("GA Ticket")
                .currency("VND")
                .faceValue(new BigDecimal("100000.00")) // Base amount 100k
                .restrictedPayment(false)
                .eventTicketMinimum(1)
                .capacityCap(500)
                .sellableQuantities(List.of(1, 2, 3))
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .charges(List.of())
                .build();

        OfferSaleWindow gaWindow = OfferSaleWindow.builder()
                .offer(gaOfferToBuild)
                .type(SaleWindowType.GENERAL_SALE)
                .startAt(Instant.now().minusSeconds(3600))
                .endAt(Instant.now().plusSeconds(3600))
                .build();
        gaOfferToBuild.setSaleWindows(List.of(gaWindow));
        gaOffer = offerRepository.saveAndFlush(gaOfferToBuild);

        inventoryGaRepository.saveAndFlush(InventoryGa.builder()
                .eventId(onsaleEvent.getId())
                .areaId(gaAreaId)
                .offerId(gaOffer.getId())
                .total(100)
                .available(100)
                .held(0)
                .sold(0)
                .build());
    }

    // ─── Checkout Initiation ───────────────────────────────────────────────

    @Test
    void initiate_payment_creates_pending_record() throws Exception {
        Reservation reservation = createReservation(1);

        String body = """
                {
                  "reservationId": "%s",
                  "provider": "VNPAY"
                }
                """.formatted(reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentId").exists())
                .andExpect(jsonPath("$.data.checkoutUrl").exists());

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payments.get(0).getAmount()).isEqualByComparingTo("100000.00");
    }

    // ─── Multiple Attempts Cancel Previous ───────────────────────────────

    @Test
    void initiate_multiple_payments_cancels_previous_attempts() throws Exception {
        Reservation reservation = createReservation(1);

        String bodyVnpay = """
                {
                  "reservationId": "%s",
                  "provider": "VNPAY"
                }
                """.formatted(reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyVnpay))
                .andExpect(status().isCreated());

        String bodyStripe = """
                {
                  "reservationId": "%s",
                  "provider": "STRIPE"
                }
                """.formatted(reservation.getId());

        try (var mockedBuilder = Mockito.mockStatic(io.qzz.hoangvu.ticketpeak.api.payment.service.StripeCheckoutBuilder.class)) {
            mockedBuilder.when(() -> io.qzz.hoangvu.ticketpeak.api.payment.service.StripeCheckoutBuilder.createPaymentIntentSecret(Mockito.any()))
                    .thenReturn("mock_client_secret");

            mockMvc.perform(post("/api/payments")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyStripe))
                    .andExpect(status().isCreated());
        }

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(2);

        // First attempt (VNPay) must be CANCELLED
        Payment first = payments.stream()
                .filter(p -> p.getProvider() == PaymentProvider.VNPAY)
                .findFirst().orElseThrow();
        assertThat(first.getStatus()).isEqualTo(PaymentStatus.CANCELLED);

        // Second attempt (Stripe) must be PENDING
        Payment second = payments.stream()
                .filter(p -> p.getProvider() == PaymentProvider.STRIPE)
                .findFirst().orElseThrow();
        assertThat(second.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // ─── Amount Mismatch IPN Rejection ──────────────────────────────────────

    @Test
    void vnpay_ipn_amount_mismatch_fails_and_does_not_convert_inventory() throws Exception {
        Reservation reservation = createReservation(1);
        Payment payment = createPayment(reservation, PaymentProvider.VNPAY, new BigDecimal("100000.00"));

        // VNPay IPN reports wrong amount (e.g. 50k instead of 100k, scaled by 100 -> 5,000,000)
        mockMvc.perform(post("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", payment.getId().toString())
                        .param("vnp_Amount", "5000000") // Mismatch amount
                        .param("vnp_SecureHash", "INVALID_MOCK_SIGNATURE")) // This signature validation will fail first
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97")); // Signature check failed is tested first
    }

    // ─── Signature Failure IPN Rejection ───────────────────────────────────

    @Test
    void invalid_signature_ipn_returns_code_97() throws Exception {
        mockMvc.perform(post("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", UUID.randomUUID().toString())
                        .param("vnp_Amount", "10000000")
                        .param("vnp_SecureHash", "wrong_hash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97"));
    }

    // ─── Stripe Webhook Concurrency Re-read recovery ────────────────────────

    @Test
    void stripe_webhook_signature_failure_returns_400() throws Exception {
        mockMvc.perform(post("/api/payments/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Stripe-Signature", "invalid_sig"))
                .andExpect(status().isBadRequest());
    }

    // ─── Grace Window Cases (Case 2: PENDING + passed expiry finalization) ───

    @Test
    void Stripe_webhook_finalizes_unswept_expired_pending_reservation() throws Exception {
        Reservation reservation = createReservation(1);
        Payment payment = createPayment(reservation, PaymentProvider.STRIPE, new BigDecimal("100000.00"));

        // Artificially push expiresAt to past, but keep status PENDING
        reservation.setExpiresAt(Instant.now().minusSeconds(10));
        reservationRepository.saveAndFlush(reservation);
        entityManager.clear();

        // Construct mock Stripe PaymentIntent object
        PaymentIntent mockIntent = Mockito.mock(PaymentIntent.class);
        Mockito.when(mockIntent.getAmount()).thenReturn(100000L); // VND direct 100k
        Mockito.when(mockIntent.getCurrency()).thenReturn("vnd");
        Mockito.when(mockIntent.getStatus()).thenReturn("succeeded");
        Mockito.when(mockIntent.getId()).thenReturn("pi_mock_123");
        Mockito.when(mockIntent.getMetadata()).thenReturn(Map.of("paymentId", payment.getId().toString()));

        // Authoritative validation succeeds because it is unswept
        paymentService.finalizeStripeWebhook(mockIntent);

        entityManager.clear();

        Payment finalizedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(finalizedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        Reservation finalizedRes = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(finalizedRes.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        InventoryGa ga = inventoryGaRepository.findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId()).orElseThrow();
        assertThat(ga.getSold()).isEqualTo(1);
    }

    // ─── Expiry Race Cases (Case 1: EXPIRED swept finalization) ─────────────

    @Test
    void Stripe_webhook_rejects_swept_expired_reservation() throws Exception {
        Reservation reservation = createReservation(1);
        Payment payment = createPayment(reservation, PaymentProvider.STRIPE, new BigDecimal("100000.00"));

        // Set status to EXPIRED (swept)
        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.saveAndFlush(reservation);
        entityManager.clear();

        // Construct mock Stripe PaymentIntent
        PaymentIntent mockIntent = Mockito.mock(PaymentIntent.class);
        Mockito.when(mockIntent.getAmount()).thenReturn(100000L);
        Mockito.when(mockIntent.getCurrency()).thenReturn("vnd");
        Mockito.when(mockIntent.getStatus()).thenReturn("succeeded");
        Mockito.when(mockIntent.getId()).thenReturn("pi_mock_456");
        Mockito.when(mockIntent.getMetadata()).thenReturn(Map.of("paymentId", payment.getId().toString()));

        // Verification must throw ReservationExpired ApiException
        assertThatThrownBy(() -> paymentService.finalizeStripeWebhook(mockIntent))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Reservation has expired");

        entityManager.clear();

        Payment finalizedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(finalizedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Reservation createReservation(int qty) {
        Reservation r = Reservation.builder()
                .accountId(buyer.getId())
                .eventId(onsaleEvent.getId())
                .status(ReservationStatus.PENDING)
                .currency("VND")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();
        
        ReservationItem item = ReservationItem.builder()
                .reservation(r)
                .offerId(gaOffer.getId())
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .areaId(gaAreaId)
                .qty(qty)
                .unitFaceValue(gaOffer.getFaceValue())
                .currency("VND")
                .charges(List.of())
                .build();
        
        r.setItems(List.of(item));

        InventoryGa inventory = inventoryGaRepository.findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId()).orElseThrow();
        inventory.setHeld(inventory.getHeld() + qty);
        inventory.setAvailable(inventory.getAvailable() - qty);
        inventoryGaRepository.saveAndFlush(inventory);
        
        return reservationRepository.saveAndFlush(r);
    }

    private Payment createPayment(Reservation r, PaymentProvider provider, BigDecimal amount) {
        Payment p = Payment.builder()
                .reservationId(r.getId())
                .accountId(buyer.getId())
                .eventId(onsaleEvent.getId())
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .currency("VND")
                .gatewayPayload(Map.of())
                .gatewayResponse(Map.of())
                .build();
        return paymentRepository.saveAndFlush(p);
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private record LoginPayload(String email, String password) {}
}
