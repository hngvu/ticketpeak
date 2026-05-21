package io.qzz.hoangvu.ticketpeak.api.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.event.dto.*;
import io.qzz.hoangvu.ticketpeak.api.event.model.*;
import io.qzz.hoangvu.ticketpeak.api.event.repository.*;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Manifest;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.ManifestRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    ManifestRepository manifestRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    ClassificationRepository classificationRepository;

    @Autowired
    EventClassificationRepository eventClassificationRepository;

    @Autowired
    EventAttractionRepository eventAttractionRepository;

    @Autowired
    EventManifestRepository eventManifestRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    Account adminAccount;
    Account ownerAccount;
    Account otherAccount;
    String adminToken;
    String ownerToken;
    String otherToken;

    Organization org;
    Venue venue;
    Manifest manifest;

    @BeforeEach
    void setup() throws Exception {
        eventManifestRepository.deleteAll();
        eventAttractionRepository.deleteAll();
        eventClassificationRepository.deleteAll();
        eventRepository.deleteAll();
        attractionRepository.deleteAll();
        classificationRepository.deleteAll();

        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        
        manifestRepository.deleteAll();
        venueRepository.deleteAll();
        
        accountRepository.deleteAll();

        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        adminAccount = accountRepository.saveAndFlush(Account.builder()
                .email("admin@ticketpeak.com")
                .password(encoded)
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());

        ownerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("owner@ticketpeak.com")
                .password(encoded)
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        otherAccount = accountRepository.saveAndFlush(Account.builder()
                .email("other@ticketpeak.com")
                .password(encoded)
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        adminToken = login("admin@ticketpeak.com", rawPassword);
        ownerToken = login("owner@ticketpeak.com", rawPassword);
        otherToken = login("other@ticketpeak.com", rawPassword);

        org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Peak Organizers")
                .slug("peak-organizers")
                .ownerAccountId(ownerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        venue = venueRepository.saveAndFlush(Venue.builder()
                .name("My Dinh Stadium")
                .address("Le Duc Tho St")
                .city("Hanoi")
                .country("Vietnam")
                .status(io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus.ACTIVE)
                .images(List.of())
                .build());

        manifest = manifestRepository.saveAndFlush(Manifest.builder()
                .id("M-001")
                .venue(venue)
                .description("Standard Layout")
                .totalCapacity(40000)
                .status(io.qzz.hoangvu.ticketpeak.api.venue.model.ManifestStatus.PUBLISHED)
                .build());
    }

    @Test
    void organizer_can_create_and_update_event() throws Exception {
        CreateEventRequest createReq = new CreateEventRequest(
                org.getId(),
                venue.getId(),
                "Sơn Tùng M-TP Concert",
                "son-tung-mtp-concert",
                "Live performance",
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(90000),
                "Asia/Ho_Chi_Minh",
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(7200),
                false,
                true,
                true,
                List.of(),
                List.of()
        );

        // 1. Create Event
        String createRes = mockMvc.perform(post("/api/partner/events")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Sơn Tùng M-TP Concert"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID eventId = UUID.fromString(objectMapper.readTree(createRes).path("data").path("id").asText());

        // 2. Update Event
        UpdateEventRequest updateReq = new UpdateEventRequest(
                venue.getId(),
                "Sơn Tùng M-TP Concert Live",
                "Updated live performance description",
                Instant.now().plusSeconds(172800),
                Instant.now().plusSeconds(180000),
                "Asia/Ho_Chi_Minh",
                Instant.now().plusSeconds(7200),
                Instant.now().plusSeconds(14400),
                true,
                true,
                false,
                List.of(),
                List.of()
        );

        mockMvc.perform(put("/api/partner/events/" + eventId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Sơn Tùng M-TP Concert Live"))
                .andExpect(jsonPath("$.data.restrictSingleSeat").value(true));
    }

    @Test
    void non_member_organizer_cannot_create_event() throws Exception {
        CreateEventRequest createReq = new CreateEventRequest(
                org.getId(),
                venue.getId(),
                "Unauthorised Concert",
                "unauthorised-concert",
                "Desc",
                Instant.now().plusSeconds(86400),
                null,
                "Asia/Ho_Chi_Minh",
                null,
                null,
                false,
                false,
                true,
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/api/partner/events")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_manage_catalogs_and_public_can_read() throws Exception {
        // 1. Admin Create Attraction
        CreateAttractionRequest attrReq = new CreateAttractionRequest(
                "Sơn Tùng M-TP",
                "son-tung-mtp",
                AttractionType.ARTIST,
                "http://img.url/sontung.jpg",
                "V-Pop King"
        );

        String attrRes = mockMvc.perform(post("/api/internal/attractions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attrReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Sơn Tùng M-TP"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID attrId = UUID.fromString(objectMapper.readTree(attrRes).path("data").path("id").asText());

        // 2. Admin Create Classification
        CreateClassificationRequest classReq = new CreateClassificationRequest(
                "Music",
                "music",
                1,
                true,
                null
        );

        String classRes = mockMvc.perform(post("/api/internal/classifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Music"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID classId = UUID.fromString(objectMapper.readTree(classRes).path("data").path("id").asText());

        // 3. Public get attractions
        mockMvc.perform(get("/api/attractions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Sơn Tùng M-TP"));

        // 4. Public get classifications
        mockMvc.perform(get("/api/classifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Music"));

        // 5. Admin and Organizer endpoints fail for public anonymous
        mockMvc.perform(post("/api/internal/attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attrReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void event_lifecycle_publish_postpone_cancel_clone() throws Exception {
        // Prepare classifications and attractions
        Attraction artist = attractionRepository.saveAndFlush(Attraction.builder()
                .name("Coldplay")
                .slug("coldplay")
                .type(AttractionType.BAND)
                .build());

        Classification genre = classificationRepository.saveAndFlush(Classification.builder()
                .name("Rock")
                .slug("rock")
                .level(1)
                .isActive(true)
                .build());

        Event event = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Coldplay Rock Show")
                .slug("coldplay-rock-show")
                .status(EventStatus.DRAFT)
                .startAt(Instant.now().plusSeconds(100000))
                .timezone("Asia/Ho_Chi_Minh")
                .build());

        eventAttractionRepository.saveAndFlush(new EventAttraction(event.getId(), artist.getId()));
        eventClassificationRepository.saveAndFlush(new EventClassification(event.getId(), genre.getId()));

        // 1. Publish (snapshot is created)
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.manifestId").value("evt-" + event.getId() + "-snap"));

        assertThat(eventManifestRepository.existsById(event.getId())).isTrue();

        // 2. Postpone
        PostponeEventRequest postponeReq = new PostponeEventRequest(
                Instant.now().plusSeconds(200000),
                Instant.now().plusSeconds(210000),
                null,
                null
        );

        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/postpone")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postponeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("POSTPONED"));

        // 3. Cancel
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/cancel")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        // 4. Clone
        CloneEventRequest cloneReq = new CloneEventRequest(
                "Coldplay Rock Show 2",
                "coldplay-rock-show-2",
                Instant.now().plusSeconds(300000),
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/clone")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cloneReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Coldplay Rock Show 2"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void search_events_full_criteria() throws Exception {
        Attraction artist = attractionRepository.saveAndFlush(Attraction.builder()
                .name("Adele")
                .slug("adele")
                .type(AttractionType.ARTIST)
                .build());

        Classification genre = classificationRepository.saveAndFlush(Classification.builder()
                .name("Pop")
                .slug("pop")
                .level(1)
                .isActive(true)
                .build());

        Event event1 = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Adele Live Hanoi")
                .slug("adele-live-hanoi")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(150000))
                .timezone("Asia/Ho_Chi_Minh")
                .build());

        eventAttractionRepository.saveAndFlush(new EventAttraction(event1.getId(), artist.getId()));
        eventClassificationRepository.saveAndFlush(new EventClassification(event1.getId(), genre.getId()));

        // Public Search by Query & Venue Location (City & Country) & Attraction
        mockMvc.perform(get("/api/events")
                        .param("query", "Adele")
                        .param("city", "Hanoi")
                        .param("country", "Vietnam")
                        .param("attractionId", artist.getId().toString())
                        .param("classificationId", genre.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Adele Live Hanoi"));
    }

    @Test
    void test_hierarchical_classification_search() throws Exception {
        Classification parent = classificationRepository.saveAndFlush(Classification.builder()
                .name("Music")
                .slug("music")
                .level(1)
                .isActive(true)
                .build());

        Classification genre = classificationRepository.saveAndFlush(Classification.builder()
                .name("Pop")
                .slug("pop")
                .level(2)
                .parentId(parent.getId())
                .isActive(true)
                .build());

        Classification subGenre = classificationRepository.saveAndFlush(Classification.builder()
                .name("K-Pop")
                .slug("k-pop")
                .level(3)
                .parentId(genre.getId())
                .isActive(true)
                .build());

        Event event = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Blackpink Born Pink")
                .slug("blackpink-born-pink")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(180000))
                .timezone("Asia/Ho_Chi_Minh")
                .build());

        eventClassificationRepository.saveAndFlush(new EventClassification(event.getId(), subGenre.getId()));

        // Query by high-level parent class: "Music"
        mockMvc.perform(get("/api/events")
                        .param("classificationId", parent.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Blackpink Born Pink"));
    }

    @Test
    void test_keyword_expansion_search() throws Exception {
        Attraction artist = attractionRepository.saveAndFlush(Attraction.builder()
                .name("Sơn Tùng M-TP")
                .slug("son-tung-mtp-artist")
                .type(AttractionType.ARTIST)
                .build());

        Classification genre = classificationRepository.saveAndFlush(Classification.builder()
                .name("V-Pop")
                .slug("v-pop")
                .level(1)
                .isActive(true)
                .build());

        Event event = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId()) // Venue is "My Dinh Stadium" in "Hanoi", "Vietnam"
                .title("Sky Tour Live")
                .slug("sky-tour-live")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(250000))
                .timezone("Asia/Ho_Chi_Minh")
                .build());

        eventAttractionRepository.saveAndFlush(new EventAttraction(event.getId(), artist.getId()));
        eventClassificationRepository.saveAndFlush(new EventClassification(event.getId(), genre.getId()));

        // 1. Search by artist name
        mockMvc.perform(get("/api/events")
                        .param("query", "Tùng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Sky Tour Live"));

        // 2. Search by venue city
        mockMvc.perform(get("/api/events")
                        .param("query", "Hanoi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Sky Tour Live"));

        // 3. Search by classification name
        mockMvc.perform(get("/api/events")
                        .param("query", "v-pop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void test_draft_event_isolation() throws Exception {
        Event draftEvent = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Secret Draft Show")
                .slug("secret-draft-show")
                .status(EventStatus.DRAFT)
                .startAt(Instant.now().plusSeconds(250000))
                .timezone("Asia/Ho_Chi_Minh")
                .build());

        // 1. Public search should NOT find the draft show
        mockMvc.perform(get("/api/events")
                        .param("query", "Secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));

        // 2. Explicitly requesting DRAFT by a public user yields empty results
        mockMvc.perform(get("/api/events")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));

        // 3. Public get by ID on a DRAFT returns 404 Not Found
        mockMvc.perform(get("/api/events/" + draftEvent.getId()))
                .andExpect(status().isNotFound());

        // 4. Partner can get DRAFT event details securely
        mockMvc.perform(get("/api/partner/events/" + draftEvent.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Secret Draft Show"));

        // 5. Non-member partner cannot get draft details
        mockMvc.perform(get("/api/partner/events/" + draftEvent.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
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

    private record LoginPayload(String email, String password) {}
}
