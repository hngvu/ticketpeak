package io.qzz.hoangvu.ticketpeak.api.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryGaRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferSaleWindow;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SaleWindowType;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferSaleWindowRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.service.ReservationService;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired EventRepository eventRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired InventoryGaRepository inventoryGaRepository;
    @Autowired InventorySeatRepository inventorySeatRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired ReservationService reservationService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired jakarta.persistence.EntityManager entityManager;

    Account buyerA;
    Account buyerB;
    String tokenA;
    String tokenB;

    Organization org;
    Venue venue;
    Event onsaleEvent;

    Offer gaOffer;
    Offer rsOffer;
    String gaAreaId = "AREA-GA-1";
    String rsSeatId = "SEAT-RS-1";

    @BeforeEach
    void setup() throws Exception {
        reservationRepository.deleteAll();
        inventoryGaRepository.deleteAll();
        inventorySeatRepository.deleteAll();
        offerRepository.deleteAll();
        eventRepository.deleteAll();
        organizationRepository.deleteAll();
        venueRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";

        buyerA = accountRepository.saveAndFlush(Account.builder()
                .email("buyerA@test.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        buyerB = accountRepository.saveAndFlush(Account.builder()
                .email("buyerB@test.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        tokenA = login(buyerA.getEmail(), rawPassword);
        tokenB = login(buyerB.getEmail(), rawPassword);

        org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Org")
                .slug("test-org")
                .ownerAccountId(buyerA.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        venue = venueRepository.saveAndFlush(Venue.builder()
                .name("Test Venue")
                .address("1 Main St")
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

        // GA offer with an active sale window
        Offer gaOfferToBuild = Offer.builder()
                .eventId(onsaleEvent.getId())
                .ticketTypeId("ga-ticket")
                .name("GA Ticket")
                .currency("VND")
                .faceValue(new BigDecimal("200000.00"))
                .restrictedPayment(false)
                .eventTicketMinimum(1)
                .capacityCap(500)
                .sellableQuantities(List.of(1, 2, 3, 4, 200))
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

        // RS offer
        Offer rsOfferToBuild = Offer.builder()
                .eventId(onsaleEvent.getId())
                .ticketTypeId("rs-ticket")
                .name("RS Ticket")
                .currency("VND")
                .faceValue(new BigDecimal("500000.00"))
                .restrictedPayment(false)
                .eventTicketMinimum(1)
                .capacityCap(100)
                .sellableQuantities(List.of(1))
                .seatingMode(SeatingMode.RESERVED_SEATING)
                .charges(List.of())
                .build();

        OfferSaleWindow rsWindow = OfferSaleWindow.builder()
                .offer(rsOfferToBuild)
                .type(SaleWindowType.GENERAL_SALE)
                .startAt(Instant.now().minusSeconds(3600))
                .endAt(Instant.now().plusSeconds(3600))
                .build();
        rsOfferToBuild.setSaleWindows(List.of(rsWindow));
        rsOffer = offerRepository.saveAndFlush(rsOfferToBuild);

        // Seed inventory
        inventoryGaRepository.saveAndFlush(InventoryGa.builder()
                .eventId(onsaleEvent.getId())
                .areaId(gaAreaId)
                .offerId(gaOffer.getId())
                .total(100)
                .available(100)
                .held(0)
                .sold(0)
                .build());

        inventorySeatRepository.saveAndFlush(InventorySeat.builder()
                .eventId(onsaleEvent.getId())
                .seatId(rsSeatId)
                .offerId(rsOffer.getId())
                .status(SeatInventoryStatus.AVAILABLE)
                .build());
    }

    // ─── Create GA reservation ────────────────────────────────────────────────

    @Test
    void create_ga_reservation_holds_inventory() throws Exception {
        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 2}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String response = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].qty").value(2))
                .andExpect(jsonPath("$.data.currency").value("VND"))
                .andReturn().getResponse().getContentAsString();

        UUID reservationId = UUID.fromString(
                objectMapper.readTree(response).path("data").path("id").asText());

        assertThat(reservationRepository.findById(reservationId)).isPresent();

        InventoryGa ga = inventoryGaRepository
                .findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId())
                .orElseThrow();
        assertThat(ga.getHeld()).isEqualTo(2);
        assertThat(ga.getAvailable()).isEqualTo(98);
    }

    // ─── Create RS reservation ────────────────────────────────────────────────

    @Test
    void create_rs_reservation_holds_seat() throws Exception {
        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "RESERVED_SEATING", "seatId": "%s"}
                  ]
                }
                """.formatted(onsaleEvent.getId(), rsOffer.getId(), rsSeatId);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].seatId").value(rsSeatId));

        InventorySeat seat = inventorySeatRepository.findById(
                new io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeatId(
                        onsaleEvent.getId(), rsSeatId)).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatInventoryStatus.HELD);
    }

    // ─── Duplicate seat hold returns 422 ──────────────────────────────────────

    @Test
    void duplicate_rs_seat_returns_error() throws Exception {
        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "RESERVED_SEATING", "seatId": "%s"}
                  ]
                }
                """.formatted(onsaleEvent.getId(), rsOffer.getId(), rsSeatId);

        // First reservation succeeds
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Second reservation for the same seat fails
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("RESERVATION_INVALID_ITEM"));
    }

    // ─── Insufficient GA capacity ─────────────────────────────────────────────

    @Test
    void ga_insufficient_capacity_returns_conflict() throws Exception {
        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 200}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("RESERVATION_GA_CAPACITY_INSUFFICIENT"));
    }

    // ─── Offer not in sale window ──────────────────────────────────────────────

    @Test
    void offer_outside_sale_window_returns_422() throws Exception {
        // Patch the sale window to be in the past
        gaOffer.getSaleWindows().get(0).setStartAt(Instant.now().minusSeconds(7200));
        gaOffer.getSaleWindows().get(0).setEndAt(Instant.now().minusSeconds(3600));
        offerRepository.saveAndFlush(gaOffer);

        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("OFFER_NOT_IN_SALE_WINDOW"));
    }

    // ─── Event not on sale ────────────────────────────────────────────────────

    @Test
    void draft_event_returns_not_on_sale() throws Exception {
        Event draftEvent = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId()).venueId(venue.getId())
                .title("Draft").slug("draft-res").status(EventStatus.DRAFT)
                .startAt(Instant.now().plusSeconds(86400)).timezone("Asia/Ho_Chi_Minh")
                .build());

        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(draftEvent.getId(), gaOffer.getId(), gaAreaId);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("EVENT_NOT_ON_SALE"));
    }

    // ─── Confirm reservation ──────────────────────────────────────────────────

    @Test
    void confirm_reservation_sells_inventory() throws Exception {
        String createBody = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reservationId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/confirm")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        InventoryGa ga = inventoryGaRepository
                .findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId())
                .orElseThrow();
        assertThat(ga.getSold()).isEqualTo(1);
        assertThat(ga.getHeld()).isEqualTo(0);
    }

    // ─── Confirm expired reservation ──────────────────────────────────────────

    @Test
    void confirm_expired_reservation_returns_410() throws Exception {
        String createBody = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID reservationId = UUID.fromString(
                objectMapper.readTree(createResponse).path("data").path("id").asText());

        // Artificially push expiry into the past
        Reservation r = reservationRepository.findById(reservationId).orElseThrow();
        r.setExpiresAt(Instant.now().minusSeconds(3600));
        reservationRepository.saveAndFlush(r);
        entityManager.clear();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/confirm")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error").value("RESERVATION_EXPIRED"));

        // Inventory should be released
        InventoryGa ga = inventoryGaRepository
                .findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId())
                .orElseThrow();
        assertThat(ga.getHeld()).isEqualTo(0);
        assertThat(ga.getAvailable()).isEqualTo(100);
    }

    // ─── Cancel reservation ───────────────────────────────────────────────────

    @Test
    void cancel_pending_reservation_releases_inventory() throws Exception {
        String createBody = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 2}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reservationId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(delete("/api/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        InventoryGa ga = inventoryGaRepository
                .findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId())
                .orElseThrow();
        assertThat(ga.getAvailable()).isEqualTo(100);
        assertThat(ga.getHeld()).isEqualTo(0);
    }

    @Test
    void cancel_confirmed_reservation_returns_conflict() throws Exception {
        String createBody = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reservationId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/confirm")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("RESERVATION_ALREADY_FINALIZED"));
    }

    // ─── Access control ───────────────────────────────────────────────────────

    @Test
    void other_user_cannot_read_reservation() throws Exception {
        String createBody = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reservationId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    void unauthenticated_request_is_rejected() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Expiry scheduler ─────────────────────────────────────────────────────

    @Test
    void expiry_job_releases_inventory_for_stale_reservations() throws Exception {
        String createBody = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 3}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID reservationId = UUID.fromString(
                objectMapper.readTree(createResponse).path("data").path("id").asText());

        // Push expiry to past
        Reservation r = reservationRepository.findById(reservationId).orElseThrow();
        r.setExpiresAt(Instant.now().minusSeconds(7200));
        reservationRepository.saveAndFlush(r);
        entityManager.clear();

        // Trigger expiry sweep
        reservationService.expireBatch(10);

        Reservation expired = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(expired.getStatus()).isEqualTo(ReservationStatus.EXPIRED);

        InventoryGa ga = inventoryGaRepository
                .findByEventIdAndAreaIdAndOfferId(onsaleEvent.getId(), gaAreaId, gaOffer.getId())
                .orElseThrow();
        assertThat(ga.getHeld()).isEqualTo(0);
        assertThat(ga.getAvailable()).isEqualTo(100);
    }

    // ─── List reservations ────────────────────────────────────────────────────

    @Test
    void list_returns_only_own_reservations() throws Exception {
        String body = """
                {
                  "eventId": "%s",
                  "items": [
                    {"offerId": "%s", "seatingMode": "GENERAL_ADMISSION", "areaId": "%s", "qty": 1}
                  ]
                }
                """.formatted(onsaleEvent.getId(), gaOffer.getId(), gaAreaId);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // BuyerB has no reservations
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        // BuyerA has one
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
