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
import io.qzz.hoangvu.ticketpeak.api.offer.dto.ChargeRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.CreateOfferRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.dto.UpdateOfferRequest;
import io.qzz.hoangvu.ticketpeak.api.offer.model.ChargeType;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    PasswordEncoder passwordEncoder;

    Account organizerAccount;
    Account buyerAccount;
    String organizerToken;
    String buyerToken;
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
        organizationRepository.deleteAll();
        venueRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";

        organizerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("organizer@ticketpeak.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        buyerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("buyer@ticketpeak.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        organizerToken = login(organizerAccount.getEmail(), rawPassword);
        buyerToken = login(buyerAccount.getEmail(), rawPassword);

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

    @Test
    void create_offer_persists_and_is_returned_by_event_lookup() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "VIP-001", "VIP Package", "Front row package",
                "VND", new BigDecimal("1250000.00"), false, 1,
                List.of(1, 2, 4),
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                Instant.now().plusSeconds(10800), Instant.now().plusSeconds(14400),
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
                .andExpect(jsonPath("$.data.quantityAvailable").value(0))
                .andExpect(jsonPath("$.data.quantitySold").value(0))
                .andExpect(jsonPath("$.data.charges.length()").value(2))
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
                "vip-dup", "Standard Ticket", null,
                "VND", new BigDecimal("250000.00"), false, 1,
                List.of(1, 2, 4), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
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
                .andExpect(jsonPath("$.error").value("OFFER_TICKET_TYPE_ALREADY_EXISTS"));
    }

    @Test
    void same_ticket_type_id_allowed_across_different_events() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "vip-cross", "VIP Cross", null,
                "VND", new BigDecimal("250000.00"), false, 1,
                List.of(1, 2, 4), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

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
        CreateOfferRequest request = new CreateOfferRequest(
                "unauth", "No Auth", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyer_cannot_create_offer() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "buyer-no", "Buyer Try", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannot_create_offer_for_canceled_event() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "canceled-no", "Canceled", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + canceledEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EVENT_STATE"));
    }

    @Test
    void cannot_create_offer_for_completed_event() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "completed-no", "Completed", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + completedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_EVENT_STATE"));
    }

    @Test
    void public_api_returns_404_for_draft_event_offers() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "draft-offer", "Draft Offer", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

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
        CreateOfferRequest createRequest = new CreateOfferRequest(
                "update-me", "Original Name", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1, 2), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateOfferRequest updateRequest = new UpdateOfferRequest(
                "Updated Name", "Updated description",
                "USD", new BigDecimal("200000.00"), true, 2, 100,
                List.of(2, 4),
                null, null, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
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
                .andExpect(jsonPath("$.data.quantityAvailable").value(100))
                .andExpect(jsonPath("$.data.sellableQuantities.length()").value(2));
    }

    @Test
    void delete_offer_removes_it() throws Exception {
        CreateOfferRequest createRequest = new CreateOfferRequest(
                "delete-me", "To Delete", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

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
    void invalid_currency_is_rejected() throws Exception {
        CreateOfferRequest request = new CreateOfferRequest(
                "bad-cur", "Bad Currency", null,
                "XYZ", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
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
                .build());

        CreateOfferRequest request = new CreateOfferRequest(
                "bad-window", "Bad Window", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(500), Instant.now().plusSeconds(8000),
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

        CreateOfferRequest request = new CreateOfferRequest(
                "scope-001", "Scoped", null,
                "VND", new BigDecimal("250000.00"), false, 1,
                List.of(1, 2, 4), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

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
        CreateOfferRequest request = new CreateOfferRequest(
                "NEG-001", "Invalid Price", null,
                "VND", new BigDecimal("-1.00"), false, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.GENERAL_ADMISSION, null, null, List.of()
        );

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
                "WIN-001", "Bad Window", null,
                "VND", new BigDecimal("100000.00"), false, 1,
                List.of(1, 2),
                Instant.now().plusSeconds(7200), Instant.now().plusSeconds(10800),
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(14400),
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
        CreateOfferRequest request = new CreateOfferRequest(
                "RS-001", "Reserved Seat", null,
                "VND", new BigDecimal("750000.00"), true, 1,
                List.of(1), null, null,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SeatingMode.RESERVED_SEATING, null, null, List.of()
        );

        mockMvc.perform(post("/api/partner/events/" + publishedEvent.getId() + "/offers")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_OFFER_SEATING"));
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
