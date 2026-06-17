package io.qzz.hoangvu.ticketpeak.api.offer;

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
import io.qzz.hoangvu.ticketpeak.api.offer.dto.*;
import io.qzz.hoangvu.ticketpeak.api.offer.model.ChargeType;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SaleWindowType;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.TicketTypeRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.*;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import io.qzz.hoangvu.ticketpeak.api.offer.service.OfferService;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OfferControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    OfferRepository offerRepository;

    @Autowired
    TicketTypeRepository ticketTypeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ManifestRepository manifestRepository;

    @Autowired
    LevelRepository levelRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    PriceLevelRepository priceLevelRepository;

    @Autowired
    OfferService offerService;

    Account organizerAccount;
    Account buyerAccount;
    String organizerToken;
    String buyerToken;
    String adminToken;
    Organization organization;
    Venue venue;
    Event publishedEvent;
    Event draftEvent;
    Event canceledEvent;
    Event completedEvent;

    @BeforeEach
    void setup() throws Exception {
        offerRepository.deleteAll();
        eventRepository.deleteAll();
        priceLevelRepository.deleteAll();
        sectionRepository.deleteAll();
        levelRepository.deleteAll();
        manifestRepository.deleteAll();
        organizationRepository.deleteAll();
        venueRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";

        organizerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("organizer@ticketpeak.com")
                .password(passwordEncoder.encode(rawPassword))
                .roles(java.util.Set.of(Role.ORGANIZER))
                .status(AccountStatus.ACTIVE)
                .build());

        buyerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("buyer@ticketpeak.com")
                .password(passwordEncoder.encode(rawPassword))
                .roles(java.util.Set.of(Role.BUYER))
                .status(AccountStatus.ACTIVE)
                .build());

        Account adminAccount = accountRepository.saveAndFlush(Account.builder()
                .email("admin@ticketpeak.com")
                .password(passwordEncoder.encode(rawPassword))
                .roles(java.util.Set.of(Role.ADMIN))
                .status(AccountStatus.ACTIVE)
                .build());

        organizerToken = login(organizerAccount.getEmail(), rawPassword);
        buyerToken = login(buyerAccount.getEmail(), rawPassword);
        adminToken = login(adminAccount.getEmail(), rawPassword);

        organization = organizationRepository.saveAndFlush(Organization.builder()
                .name("Offer Org")
                .slug("offer-org")
                .ownerAccountId(organizerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        venue = venueRepository.saveAndFlush(Venue.builder()
                .name("Offer Arena")
                .address("123 Main Street")
                .city("Hanoi")
                .country("Vietnam")
                .status(VenueStatus.ACTIVE)
                .images(List.of())
                .build());

        publishedEvent = saveEvent("offer-show-published", "Published Show", EventStatus.PUBLISHED);
        draftEvent = saveEvent("offer-show-draft", "Draft Show", EventStatus.DRAFT);
        canceledEvent = saveEvent("offer-show-canceled", "Canceled Show", EventStatus.CANCELED);
        completedEvent = saveEvent("offer-show-completed", "Completed Show", EventStatus.COMPLETED);
    }

    private CreateSaleWindowRequest createSaleWindowRequest(SaleWindowType type, int startOffsetSeconds, int endOffsetSeconds) {
        return new CreateSaleWindowRequest(type, Instant.now().plusSeconds(startOffsetSeconds), Instant.now().plusSeconds(endOffsetSeconds));
    }

    private UpdateSaleWindowRequest updateSaleWindowRequest(SaleWindowType type, int startOffsetSeconds, int endOffsetSeconds) {
        return new UpdateSaleWindowRequest(type, Instant.now().plusSeconds(startOffsetSeconds), Instant.now().plusSeconds(endOffsetSeconds));
    }

    

    private CreateOfferRequest createOfferRequest(java.util.UUID ticketTypeId, String name) {
        return createOfferRequest(ticketTypeId, name, new BigDecimal("100000.00"), SeatingMode.GENERAL_ADMISSION, List.of());
    }

    
    private java.util.UUID createTicketType(java.util.UUID eventId, String code) {
        return ticketTypeRepository.saveAndFlush(TicketType.builder()
                .eventId(eventId)
                .name(code)
                .code(code.toUpperCase().replace("-", "_"))
                .build()).getId();
    }
    private CreateOfferRequest createOfferRequest(java.util.UUID ticketTypeId, String name, BigDecimal faceValue, SeatingMode seatingMode, List<CreateSaleWindowRequest> windows) {
        return new CreateOfferRequest(
                ticketTypeId, "CODE-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(), name, null,
                "VND", faceValue, false, 1,
                100,
                List.of(1, 2, 4), windows,
                seatingMode, null, null, List.of()
        );
    }

    @Test
    void create_offer_persists_and_is_returned_by_event_lookup() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "VIP-001"), "VIP-PKG-01", "VIP Package", "Front row package",
                "VND", new BigDecimal("1250000.00"), false, 1,
                100,
                List.of(1, 2, 4),
                List.of(
                        createSaleWindowRequest(SaleWindowType.PRESALE, 3600, 7200),
                        createSaleWindowRequest(SaleWindowType.GENERAL_SALE, 10800, 14400)
                ),
                SeatingMode.GENERAL_ADMISSION, null, null,
                List.of(
                        new ChargeRequest("Service fee", ChargeType.FEE, new BigDecimal("50000.00"), false),
                        new ChargeRequest("VAT", ChargeType.TAX, new BigDecimal("125000.00"), false)
                )
        );

        String response = mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketTypeId").value("vip-001"))
                .andExpect(jsonPath("$.data.faceValue").value(1250000.00))
                .andExpect(jsonPath("$.data.sellableQuantities[0]").value(1))
                .andExpect(jsonPath("$.data.capacityCap").value(100))
                .andExpect(jsonPath("$.data.charges.length()").value(2))
                .andExpect(jsonPath("$.data.saleWindows.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdBody = objectMapper.readTree(response);
        UUID offerId = UUID.fromString(createdBody.path("data").path("id").asText());

        Offer persisted = offerRepository.findById(offerId).orElseThrow();
        assertThat(persisted.getEventId()).isEqualTo(publishedEvent.getId());
        assertThat(persisted.getTicketTypeId()).isEqualTo("vip-001");
        assertThat(persisted.getSellableQuantities()).containsExactly(1, 2, 4);
        assertThat(persisted.getCharges()).hasSize(2);
        assertThat(persisted.getSaleWindows()).hasSize(2);

        mockMvc.perform(get("/api/events/" + publishedEvent.getId() + "/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ticketTypeId").value("vip-001"));

        mockMvc.perform(get("/api/events/" + publishedEvent.getId() + "/offers/vip-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("VIP Package"))
                .andExpect(jsonPath("$.data.seatingMode").value("GENERAL_ADMISSION"));
    }

    @Test
    void duplicate_ticket_type_id_within_same_event_is_rejected() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "vip-dup"), "STD-CODE", "Standard Ticket", null,
                "VND", new BigDecimal("250000.00"), false, 1,
                100,
                List.of(1, 2, 4),
                List.of(createSaleWindowRequest(SaleWindowType.GENERAL_SALE, 3600, 7200)),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("OFFER_CODE_ALREADY_EXISTS"));
    }

    @Test
    void duplicate_code_within_same_event_is_rejected() throws Exception {
        CreateOfferRequest request1 = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "tt-1"), "DUP-CODE", "Standard Ticket", null,
                "VND", new BigDecimal("250000.00"), false, 1,
                100,
                List.of(1, 2, 4),
                List.of(createSaleWindowRequest(SaleWindowType.GENERAL_SALE, 3600, 7200)),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CreateOfferRequest request2 = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "tt-2"), "DUP-CODE", "Another Ticket", null,
                "VND", new BigDecimal("350000.00"), false, 1,
                100,
                List.of(1, 2, 4),
                List.of(createSaleWindowRequest(SaleWindowType.GENERAL_SALE, 3600, 7200)),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("OFFER_CODE_ALREADY_EXISTS"));
    }

    @Test
    void same_ticket_type_id_allowed_across_different_events() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "vip-cross"), "VIP Cross");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void unauthorized_requests_are_rejected() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "unauth"), "No Auth");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyer_cannot_create_offer() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "buyer-no"), "Buyer Try");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_create_offer_for_canceled_event() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "canceled-no"), "Canceled");

        mockMvc.perform(post("/api/partner/events/" + canceledEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EVENT_STATE"));
    }

    @Test
    void cannot_create_offer_for_completed_event() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "completed-no"), "Completed");

        mockMvc.perform(post("/api/partner/events/" + completedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EVENT_STATE"));
    }

    @Test
    void public_api_returns_404_for_draft_event_offers() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "draft-offer"), "Draft Offer");

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/events/" + draftEvent.getId() + "/offers"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EVENT_NOT_FOUND"));
    }

    @Test
    void update_offer_modifies_fields() throws Exception {
        CreateOfferRequest createRequest = createOfferRequest(createTicketType(publishedEvent.getId(), "update-me"), "Original Name");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateOfferRequest updateRequest = new UpdateOfferRequest(
                "UPDATED-CODE", "Updated Name", "Updated description",
                "USD", new BigDecimal("200000.00"), true, 2, 100,
                List.of(2, 4),
                List.of(updateSaleWindowRequest(SaleWindowType.GENERAL_SALE, 3600, 7200)),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(put("/api/partner/events/" + publishedEvent.getId() + "/offers/update-me")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.faceValue").value(200000.00))
                .andExpect(jsonPath("$.data.restrictedPayment").value(true))
                .andExpect(jsonPath("$.data.eventTicketMinimum").value(2))
                .andExpect(jsonPath("$.data.capacityCap").value(100))
                .andExpect(jsonPath("$.data.sellableQuantities.length()").value(2))
                .andExpect(jsonPath("$.data.saleWindows.length()").value(1));
    }

    @Test
    void delete_offer_removes_it() throws Exception {
        CreateOfferRequest createRequest = createOfferRequest(createTicketType(publishedEvent.getId(), "delete-me"), "To Delete");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/events/" + publishedEvent.getId() + "/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(delete("/api/partner/events/" + publishedEvent.getId() + "/offers/delete-me")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/" + publishedEvent.getId() + "/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void cannot_delete_offer_for_canceled_or_completed_event() throws Exception {
        CreateOfferRequest createRequest = createOfferRequest(createTicketType(publishedEvent.getId(), "del-state"), "Delete State Test");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Change event to CANCELED
        publishedEvent.setStatus(EventStatus.CANCELED);
        eventRepository.saveAndFlush(publishedEvent);

        mockMvc.perform(delete("/api/partner/events/" + publishedEvent.getId() + "/offers/del-state")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EVENT_STATE"));

        // Change event to COMPLETED
        publishedEvent.setStatus(EventStatus.COMPLETED);
        eventRepository.saveAndFlush(publishedEvent);

        mockMvc.perform(delete("/api/partner/events/" + publishedEvent.getId() + "/offers/del-state")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EVENT_STATE"));
    }


    @Test
    void invalid_currency_is_rejected() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "bad-cur"), "BAD-CUR-CODE", "Bad Currency", null,
                "XYZ", new BigDecimal("100000.00"), false, 1,
                100,
                List.of(1), List.of(),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CURRENCY"));
    }

    @Test
    void sale_window_outside_event_window_is_rejected() throws Exception {
        Event eventWithWindow = eventRepository.saveAndFlush(Event.builder()
                .organizationId(organization.getId())
                .venueId(venue.getId())
                .title("Windowed Event")
                .slug("windowed-event")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(86400))
                .timezone("Asia/Ho_Chi_Minh")
                .saleStartAt(Instant.now().plusSeconds(3600))
                .saleEndAt(Instant.now().plusSeconds(7200))
                .serviceFeePercent(BigDecimal.ZERO)
                .build());

        CreateOfferRequest request = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "bad-window"), "BAD-WIN-CODE", "Bad Window", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                100,
                List.of(1),
                List.of(createSaleWindowRequest(SaleWindowType.GENERAL_SALE, 500, 8000)),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + eventWithWindow.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_OFFER_WINDOW"));
    }

    @Test
    void event_scoping_keeps_offers_isolated() throws Exception {
        Event secondPublished = saveEvent("scope-two", "Scope Two", EventStatus.PUBLISHED);

        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "scope-001"), "Scoped");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/events/" + secondPublished.getId() + "/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/events/" + secondPublished.getId() + "/offers/scope-001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("OFFER_NOT_FOUND"));
    }

    @Test
    void negative_price_is_rejected_by_validation() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "NEG-001"), "Invalid Price", new BigDecimal("-1.00"), SeatingMode.GENERAL_ADMISSION, List.of());

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.faceValue[0]").value("must be greater than or equal to 0"));
    }

    @Test
    void impossible_sale_window_is_rejected() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "WIN-001"), "IMP-WIN-CODE", "Bad Window", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                100,
                List.of(1, 2),
                List.of(
                        createSaleWindowRequest(SaleWindowType.PRESALE, 7200, 10800),
                        createSaleWindowRequest(SaleWindowType.GENERAL_SALE, 3600, 14400)
                ),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_OFFER_WINDOW"));
    }

    @Test
    void reserved_seating_requires_section_and_price_level() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "RS-001"), "Reserved Seat", new BigDecimal("750000.00"), SeatingMode.RESERVED_SEATING, List.of());

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_OFFER_SEATING"));
    }

    // Obsolete check on quantityAvailable < quantitySold removed as those fields are refactored to capacityCap

    @Test
    void partner_can_list_all_offers_for_event() throws Exception {
        CreateOfferRequest firstOffer = createOfferRequest(createTicketType(publishedEvent.getId(), "tier-one"), "Tier One");
        CreateOfferRequest secondOffer = createOfferRequest(createTicketType(publishedEvent.getId(), "tier-two"), "Tier Two");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOffer)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOffer)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].ticketTypeId").value("tier-one"))
                .andExpect(jsonPath("$.data[1].ticketTypeId").value("tier-two"));
    }

    @Test
    void partner_can_get_single_offer_even_if_draft() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "draft-single"), "Draft Single");

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Partner can view it
        mockMvc.perform(get("/api/partner/events/" + draftEvent.getId() + "/offers/draft-single")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketTypeId").value("draft-single"))
                .andExpect(jsonPath("$.data.name").value("Draft Single"));

        // Public cannot view it
        mockMvc.perform(get("/api/events/" + draftEvent.getId() + "/offers/draft-single"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EVENT_NOT_FOUND"));
    }

    @Test
    void admin_can_bypass_organizer_check_to_create_offer() throws Exception {
        CreateOfferRequest request = createOfferRequest(createTicketType(publishedEvent.getId(), "admin-bypass"), "Admin Bypass Offer");

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketTypeId").value("admin-bypass"));
    }

    @Test
    void reserved_seating_offer_validates_section_and_price_level() throws Exception {
        CreateOfferRequest requestNoManifest = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "rs-no-man"), "RS-NO-MAN-CODE", "RS No Manifest", null,
                "VND", new BigDecimal("500000.00"), false, 1,
                100,
                List.of(1), null,
                SeatingMode.RESERVED_SEATING, "SEC-A", "PL-1", List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestNoManifest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NO_PUBLISHED_MANIFEST"));

        Manifest manifest = manifestRepository.saveAndFlush(Manifest.builder()
                .id("M-OFFER-1")
                .venue(venue)
                
                .totalCapacity(1000)
                .status(ManifestStatus.PUBLISHED)
                .build());

        levelRepository.saveAndFlush(Level.builder().id("LV-1").manifest(manifest).build());
        sectionRepository.saveAndFlush(Section.builder().id("SEC-A").manifest(manifest).build());
        priceLevelRepository.saveAndFlush(PriceLevel.builder().id("PL-1").manifest(manifest).build());

        CreateOfferRequest requestValid = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "rs-valid"), "RS-VALID-CODE", "RS Valid", null,
                "VND", new BigDecimal("500000.00"), false, 1,
                100,
                List.of(1), null,
                SeatingMode.RESERVED_SEATING, "SEC-A", "PL-1", List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketTypeId").value("rs-valid"));

        CreateOfferRequest requestInvalidSection = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "rs-invalid-sec"), "RS-INV-SEC", "RS Invalid Sec", null,
                "VND", new BigDecimal("500000.00"), false, 1,
                100,
                List.of(1), null,
                SeatingMode.RESERVED_SEATING, "SEC-INVALID", "PL-1", List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalidSection)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SECTION_NOT_FOUND"));

        CreateOfferRequest requestInvalidPL = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "rs-invalid-pl"), "RS-INV-PL", "RS Invalid PL", null,
                "VND", new BigDecimal("500000.00"), false, 1,
                100,
                List.of(1), null,
                SeatingMode.RESERVED_SEATING, "SEC-A", "PL-INVALID", List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + draftEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalidPL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PRICE_LEVEL_NOT_FOUND"));
    }



    @Test
    void service_layer_programmatic_validations_enforced() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin", "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );
        try {
            // 1. Check faceValue < 0
            CreateOfferRequest negativePriceReq = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "NEG-SVC"), "NEG-PR-CODE", "Invalid Price Service", null,
                    "VND", new BigDecimal("-50.00"), false, 1,
                    100,
                    List.of(1, 2), null,
                    SeatingMode.GENERAL_ADMISSION, null, null, List.of()
            );
            assertThatExceptionOfType(ApiException.class)
                    .isThrownBy(() -> offerService.createOffer(publishedEvent.getId(), negativePriceReq))
                    .satisfies(ex -> {
                        assertThat(ex.getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
                        assertThat(ex.getErrorCode()).isEqualTo("INVALID_OFFER_PRICE");
                    });

            // 2. Check eventTicketMinimum < 1
            CreateOfferRequest badMinLimitReq = new CreateOfferRequest(
                createTicketType(publishedEvent.getId(), "MIN-SVC"), "MIN-LMT-CODE", "Invalid Min Service", null,
                    "VND", new BigDecimal("1000.00"), false, 0,
                    100,
                    List.of(1, 2), null,
                    SeatingMode.GENERAL_ADMISSION, null, null, List.of()
            );
            assertThatExceptionOfType(ApiException.class)
                    .isThrownBy(() -> offerService.createOffer(publishedEvent.getId(), badMinLimitReq))
                    .satisfies(ex -> {
                        assertThat(ex.getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
                        assertThat(ex.getErrorCode()).isEqualTo("INVALID_OFFER_LIMITS");
                    });
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }


    private Event saveEvent(String slug, String title, EventStatus status) {
        return eventRepository.saveAndFlush(Event.builder()
                .organizationId(organization.getId())
                .venueId(venue.getId())
                .title(title)
                .slug(slug)
                .status(status)
                .startAt(Instant.now().plusSeconds(86400))
                .timezone("Asia/Ho_Chi_Minh")
                .serviceFeePercent(BigDecimal.ZERO)
                .build());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private record LoginPayload(String email, String password) {
    }
}
