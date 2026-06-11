package io.qzz.hoangvu.ticketpeak.api.eventgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.dto.*;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroup;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.repository.EventGroupMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.repository.EventGroupRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
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
class EventGroupControllerIT {

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
    EventRepository eventRepository;

    @Autowired
    EventGroupRepository eventGroupRepository;

    @Autowired
    EventGroupMemberRepository eventGroupMemberRepository;

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
    Event event1;
    Event event2;
    Event draftEvent;

    @BeforeEach
    void setup() throws Exception {
        eventGroupMemberRepository.deleteAll();
        eventGroupRepository.deleteAll();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        adminAccount = accountRepository.saveAndFlush(Account.builder()
                .email("admin@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.ADMIN))
                .status(AccountStatus.ACTIVE)
                .build());

        ownerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("owner@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.ORGANIZER))
                .status(AccountStatus.ACTIVE)
                .build());

        otherAccount = accountRepository.saveAndFlush(Account.builder()
                .email("other@ticketpeak.com")
                .password(encoded)
                .roles(java.util.Set.of(Role.ORGANIZER))
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

        event1 = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Coldplay Night 1")
                .slug("coldplay-night-1")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(100000))
                .timezone("Asia/Ho_Chi_Minh")
                .serviceFeePercent(java.math.BigDecimal.ZERO)
                .resaleEnabled(false)
                .maxResaleListingsPerUser(1)
                .build());

        event2 = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Coldplay Night 2")
                .slug("coldplay-night-2")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(200000))
                .timezone("Asia/Ho_Chi_Minh")
                .serviceFeePercent(java.math.BigDecimal.ZERO)
                .resaleEnabled(false)
                .maxResaleListingsPerUser(1)
                .build());

        draftEvent = eventRepository.saveAndFlush(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Coldplay Secret Draft")
                .slug("coldplay-secret-draft")
                .status(EventStatus.DRAFT)
                .startAt(Instant.now().plusSeconds(300000))
                .timezone("Asia/Ho_Chi_Minh")
                .serviceFeePercent(java.math.BigDecimal.ZERO)
                .resaleEnabled(false)
                .maxResaleListingsPerUser(1)
                .build());
    }

    @Test
    void partner_can_manage_event_group_lifecycle() throws Exception {
        // 1. Create Event Group
        CreateEventGroupRequest createReq = new CreateEventGroupRequest(
                org.getId(),
                "Coldplay World Tour 2026",
                "coldplay-world-tour-2026",
                "Coldplay Asian Tour details",
                "http://img.url/coldplay.jpg",
                List.of(event1.getId(), event2.getId())
        );

        String createRes = mockMvc.perform(post("/api/partner/event-groups")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Coldplay World Tour 2026"))
                .andExpect(jsonPath("$.data.events.length()").value(2))
                .andExpect(jsonPath("$.data.events[0].title").value("Coldplay Night 1"))
                .andExpect(jsonPath("$.data.events[1].title").value("Coldplay Night 2"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID groupId = UUID.fromString(objectMapper.readTree(createRes).path("data").path("id").asText());

        // 2. Retrieve Event Group via Public endpoint (drafts filtered out)
        mockMvc.perform(get("/api/event-groups/" + groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Coldplay World Tour 2026"))
                .andExpect(jsonPath("$.data.events.length()").value(2));

        // 3. Update Event Group (add draftEvent, change name)
        UpdateEventGroupRequest updateReq = new UpdateEventGroupRequest(
                "Coldplay World Tour 2026 (Updated)",
                "coldplay-world-tour-2026",
                "Updated details",
                "http://img.url/coldplay-updated.jpg",
                true,
                List.of(event1.getId(), event2.getId(), draftEvent.getId())
        );

        mockMvc.perform(put("/api/partner/event-groups/" + groupId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Coldplay World Tour 2026 (Updated)"))
                .andExpect(jsonPath("$.data.events.length()").value(3));

        // 4. Public view should filter out DRAFT event
        mockMvc.perform(get("/api/event-groups/" + groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events.length()").value(2))
                .andExpect(jsonPath("$.data.events[0].title").value("Coldplay Night 1"))
                .andExpect(jsonPath("$.data.events[1].title").value("Coldplay Night 2"));

        // 5. Remove event from group
        mockMvc.perform(delete("/api/partner/event-groups/" + groupId + "/events/" + event2.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/event-groups/" + groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events.length()").value(1))
                .andExpect(jsonPath("$.data.events[0].title").value("Coldplay Night 1"));

        // 6. Delete Event Group
        mockMvc.perform(delete("/api/partner/event-groups/" + groupId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/event-groups/" + groupId))
                .andExpect(status().isNotFound());
    }

    @Test
    void non_member_organizer_cannot_manage_group() throws Exception {
        CreateEventGroupRequest createReq = new CreateEventGroupRequest(
                org.getId(),
                "Other Tour",
                "other-tour",
                "Desc",
                null,
                List.of()
        );

        mockMvc.perform(post("/api/partner/event-groups")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_has_full_override_rights_via_ops() throws Exception {
        EventGroup group = eventGroupRepository.saveAndFlush(EventGroup.builder()
                .id(UUID.randomUUID())
                .organizationId(org.getId())
                .name("Admin Managed Series")
                .slug("admin-managed-series")
                .build());

        UpdateEventGroupRequest updateReq = new UpdateEventGroupRequest(
                "Admin Overridden Title",
                "admin-managed-series",
                "Desc",
                null,
                true,
                List.of()
        );

        mockMvc.perform(put("/api/ops/event-groups/" + group.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Admin Overridden Title"));
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
