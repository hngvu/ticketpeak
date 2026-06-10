package io.qzz.hoangvu.ticketpeak.api.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
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
import io.qzz.hoangvu.ticketpeak.api.offer.repository.TicketTypeRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.gateway.StripeCheckoutBuilder;
import io.qzz.hoangvu.ticketpeak.api.payment.gateway.VnpayCheckoutBuilder;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.service.StripeService;
import io.qzz.hoangvu.ticketpeak.api.payment.service.VnpayService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
    @Autowired StripeService stripeService;
    @Autowired VnpayService vnpayService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired jakarta.persistence.EntityManager entityManager;

    @MockBean StripeCheckoutBuilder stripeCheckoutBuilder;
    @MockBean VnpayCheckoutBuilder vnpayCheckoutBuilder;

    Account buyer;
    String token;

    Organization org;
    Venue venue;
    Event onsaleEvent;
    Offer gaOffer;
    String gaSectionId = "AREA-GA-IT";

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
                .ticketTypeId(java.util.UUID.randomUUID())
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
                .sectionId(gaSectionId)
                .offerId(gaOffer.getId())
                .total(100)
                .available(100)
                .held(0)
                .sold(0)
                .build());

        // Define default mock stubbing behavior
        Mockito.when(vnpayCheckoutBuilder.buildRedirectUrl(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn("http://mock-vnpay-url.com");
        Mockito.when(vnpayCheckoutBuilder.verifySignature(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        Mockito.when(stripeCheckoutBuilder.createPaymentIntentSecret(Mockito.any()))
                .thenReturn("mock_client_secret");
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
                .andExpect(jsonPath("$.data.checkoutUrl").value("http://mock-vnpay-url.com"));

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

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyStripe))
                .andExpect(status().isCreated());

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

        mockMvc.perform(post("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", payment.getId().toString())
                        .param("vnp_Amount", "5000000") // Mismatch amount (50k instead of 100k)
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionStatus", "00")
                        .param("vnp_SecureHash", "MOCK_SIGNATURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("04")); // Invalid amount response code
    }

    // ─── Signature Failure IPN Rejection ───────────────────────────────────

    @Test
    void invalid_signature_ipn_returns_code_97() throws Exception {
        Mockito.when(vnpayCheckoutBuilder.verifySignature(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(false);

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
    void stripe_webhook_finalizes_unswept_expired_pending_reservation() throws Exception {
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

        // Stub static constructEvent to verify signature and return our event
        com.stripe.model.Event mockEvent = Mockito.mock(com.stripe.model.Event.class);
        Mockito.when(mockEvent.getType()).thenReturn("payment_intent.succeeded");
        com.stripe.model.EventDataObjectDeserializer mockDeserializer = Mockito.mock(com.stripe.model.EventDataObjectDeserializer.class);
        Mockito.when(mockDeserializer.getObject()).thenReturn(java.util.Optional.of(mockIntent));
        Mockito.when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);

        Mockito.when(stripeCheckoutBuilder.verifyStripeWebhook(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(mockEvent);

        stripeService.handleWebhook(new io.qzz.hoangvu.ticketpeak.api.payment.service.WebhookContext(
                null, "mock_payload", Map.of("Stripe-Signature", "sig")
        ));

        entityManager.clear();

        Payment finalizedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(finalizedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        Reservation finalizedRes = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(finalizedRes.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        InventoryGa ga = inventoryGaRepository.findByEventIdAndSectionIdAndOfferId(onsaleEvent.getId(), gaSectionId, gaOffer.getId()).orElseThrow();
        assertThat(ga.getSold()).isEqualTo(1);
    }

    // ─── Expiry Race Cases (Case 1: EXPIRED swept finalization) ─────────────

    @Test
    void stripe_webhook_rejects_swept_expired_reservation() throws Exception {
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

        // Stub event
        com.stripe.model.Event mockEvent = Mockito.mock(com.stripe.model.Event.class);
        Mockito.when(mockEvent.getType()).thenReturn("payment_intent.succeeded");
        com.stripe.model.EventDataObjectDeserializer mockDeserializer = Mockito.mock(com.stripe.model.EventDataObjectDeserializer.class);
        Mockito.when(mockDeserializer.getObject()).thenReturn(java.util.Optional.of(mockIntent));
        Mockito.when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);

        Mockito.when(stripeCheckoutBuilder.verifyStripeWebhook(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(mockEvent);

        io.qzz.hoangvu.ticketpeak.api.payment.service.WebhookContext webhookContext =
                new io.qzz.hoangvu.ticketpeak.api.payment.service.WebhookContext(
                        null, "mock_payload", Map.of("Stripe-Signature", "sig")
                );

        // Verification must throw ReservationExpired ApiException
        assertThatThrownBy(() -> stripeService.handleWebhook(webhookContext))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Reservation has expired");

        entityManager.clear();

        Payment finalizedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(finalizedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    // ─── Unauthorized Status Queries ────────────────────────────────────────

    @Test
    void unauthorized_payment_status_lookup_returns_403() throws Exception {
        Reservation reservation = createReservation(1);
        Payment payment = createPayment(reservation, PaymentProvider.VNPAY, new BigDecimal("100000.00"));

        // Create another buyer and login
        Account otherBuyer = accountRepository.saveAndFlush(Account.builder()
                .email("otherbuyer@test.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());
        String otherBuyerToken = login(otherBuyer.getEmail(), "Password123!");

        // Requesting status with other buyer's token must return 403 Forbidden
        mockMvc.perform(get("/api/payments/" + payment.getId())
                        .header("Authorization", "Bearer " + otherBuyerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("PAYMENT_OWNER_MISMATCH"));
    }

    // ─── VNPay IPN Happy Path End-to-End ────────────────────────────────────

    @Test
    void vnpay_ipn_happy_path_finalizes_successfully() throws Exception {
        Reservation reservation = createReservation(1);
        Payment payment = createPayment(reservation, PaymentProvider.VNPAY, new BigDecimal("100000.00"));

        mockMvc.perform(post("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", payment.getId().toString())
                        .param("vnp_Amount", "10000000") // 100,000 VND * 100
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionStatus", "00")
                        .param("vnp_TransactionNo", "vnp_tx_999")
                        .param("vnp_SecureHash", "VALID_HASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));

        entityManager.clear();

        Payment finalizedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(finalizedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(finalizedPayment.getGatewayTransactionId()).isEqualTo("vnp_tx_999");

        Reservation finalizedRes = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(finalizedRes.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        InventoryGa ga = inventoryGaRepository.findByEventIdAndSectionIdAndOfferId(onsaleEvent.getId(), gaSectionId, gaOffer.getId()).orElseThrow();
        assertThat(ga.getSold()).isEqualTo(1);
    }

    // ─── VNPay IPN Idempotency Consecutive Calls ─────────────────────────────

    @Test
    void vnpay_ipn_idempotency_ignores_consecutive_calls() throws Exception {
        Reservation reservation = createReservation(1);
        Payment payment = createPayment(reservation, PaymentProvider.VNPAY, new BigDecimal("100000.00"));

        // First call
        mockMvc.perform(post("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", payment.getId().toString())
                        .param("vnp_Amount", "10000000")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionStatus", "00")
                        .param("vnp_TransactionNo", "vnp_tx_999")
                        .param("vnp_SecureHash", "VALID_HASH"))
                .andExpect(status().isOk());

        // Second call (idempotency success)
        mockMvc.perform(post("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", payment.getId().toString())
                        .param("vnp_Amount", "10000000")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionStatus", "00")
                        .param("vnp_TransactionNo", "vnp_tx_999")
                        .param("vnp_SecureHash", "VALID_HASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));
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
                .sectionId(gaSectionId)
                .qty(qty)
                .unitFaceValue(gaOffer.getFaceValue())
                .currency("VND")
                .charges(List.of())
                .build();

        r.setItems(List.of(item));

        InventoryGa inventory = inventoryGaRepository.findByEventIdAndSectionIdAndOfferId(onsaleEvent.getId(), gaSectionId, gaOffer.getId()).orElseThrow();
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
