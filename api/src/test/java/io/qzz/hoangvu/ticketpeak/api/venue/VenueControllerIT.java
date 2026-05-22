package io.qzz.hoangvu.ticketpeak.api.venue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.*;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.ManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VenueControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ManifestRepository manifestRepository;

    @Autowired
    private io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository venueRepository;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        manifestRepository.deleteAll();
        venueRepository.deleteAll();
        accountRepository.deleteAll();
        accountRepository.flush();
        venueRepository.flush();
        manifestRepository.flush();

        String rawPassword = "password123";
        Account adminAccount = accountRepository.saveAndFlush(Account.builder()
                .email("admin@tp.com")
                .password(passwordEncoder.encode(rawPassword))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());

        adminToken = login("admin@tp.com", rawPassword);
    }

    @Test
    void admin_can_create_and_get_venue() throws Exception {
        CreateVenueRequest req = new CreateVenueRequest(
                "Saigon Hall", "1 Le Duan", "Ho Chi Minh City", "VN",
                null, null, null, null, null, null, null, null);

        String responseStr = mockMvc.perform(post("/api/internal/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(get("/api/venues/" + venueId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Saigon Hall"));
    }

    @Test
    void venue_deactivate_and_activate() throws Exception {
        String responseStr = mockMvc.perform(post("/api/internal/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("Arena", "2 Street", "HCM", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void manifest_lifecycle_at_most_one_published() throws Exception {
        String responseStr = mockMvc.perform(post("/api/internal/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("Test Venue", "Addr", "City", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M001", "Layout A", 1000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M002", "Layout B", 500))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/internal/venues/manifests/M001/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(post("/api/internal/venues/manifests/M002/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        assertThat(manifestRepository.findById("M001").orElseThrow().getStatus().name()).isEqualTo("ARCHIVED");
    }

    @Test
    void cannot_publish_non_draft_manifest() throws Exception {
        String responseStr = mockMvc.perform(post("/api/internal/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M003", "L", 100))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/internal/venues/manifests/M003/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/internal/venues/manifests/M003/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_MANIFEST_STATUS"));
    }

    @Test
    void lookup_tables_upsert_and_list() throws Exception {
        String responseStr = mockMvc.perform(post("/api/internal/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M004", "L", 100))))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/internal/venues/manifests/M004/levels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Floor 1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Floor 1"));

        mockMvc.perform(get("/api/internal/venues/manifests/M004/levels")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void seat_uniqueness_enforced() throws Exception {
        String responseStr = mockMvc.perform(post("/api/internal/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M005", "L", 100))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/internal/venues/manifests/M005/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRSAreaRequest("RA001", "L1", "S1", "P1"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/internal/venues/rs-areas/RA001/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW001", "A", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/internal/venues/rows/ROW001/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("S001", "1", null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/internal/venues/rows/ROW001/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("S002", "1", null, null, null, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_NAME_DUPLICATE"));
    }

    @Test
    void manifest_clone_copies_hierarchy() throws Exception {
        String responseStr = mockMvc.perform(post("/api/internal/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/internal/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M006", "Original", 100))))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/internal/venues/manifests/M006/levels").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Level 1"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/internal/venues/manifests/M006/clone").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CloneManifestRequest("M006-CLONE", "Clone Layout"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("M006-CLONE"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(get("/api/internal/venues/manifests/M006-CLONE/levels").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void non_admin_cannot_access_internal_venues() throws Exception {
        Account organizerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("organizer@tp.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Organizer")
                .lastName("User")
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        String organizerToken = login("organizer@tp.com", "password123");

        CreateVenueRequest req = new CreateVenueRequest(
                "Saigon Hall", "1 Le Duan", "Ho Chi Minh City", "VN",
                null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/internal/venues")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
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
