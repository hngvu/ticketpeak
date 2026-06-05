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

        String responseStr = mockMvc.perform(post("/api/ops/venues")
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
        String responseStr = mockMvc.perform(post("/api/ops/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("Arena", "2 Street", "HCM", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void manifest_lifecycle_at_most_one_published() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("Test Venue", "Addr", "City", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M001", "Layout A", 1000, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M002", "Layout B", 500, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ops/venues/manifests/M001/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(post("/api/ops/venues/manifests/M002/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        assertThat(manifestRepository.findById("M001").orElseThrow().getStatus().name()).isEqualTo("ARCHIVED");
    }

    @Test
    void cannot_publish_non_draft_manifest() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M003", "L", 100, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ops/venues/manifests/M003/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/ops/venues/manifests/M003/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_MANIFEST_STATUS"));
    }

    @Test
    void lookup_tables_upsert_and_list() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M004", "L", 100, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/ops/venues/manifests/M004/levels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Floor 1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Floor 1"));

        mockMvc.perform(get("/api/ops/venues/manifests/M004/levels")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void seat_uniqueness_enforced() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M005", "L", 100, null))))
                .andExpect(status().isCreated());
                mockMvc.perform(post("/api/ops/venues/manifests/M005/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRSAreaRequest("RA001", "L1", null, null, null, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/ops/venues/rs-areas/RA001/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW001", "A", null))))
                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/ops/venues/rows/ROW001/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("S001", "1", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ops/venues/rows/ROW001/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("S002", "1", null, null, null, null, null, null, null, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_NAME_DUPLICATE"));
    }

    @Test
    void seat_row_uniqueness_enforced() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V-Row", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M005-Row", "L", 100, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/ops/venues/manifests/M005-Row/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRSAreaRequest("RA001-Row", "L1", null, null, null, null))))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/ops/venues/rs-areas/RA001-Row/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW001-Row", "Row-A", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ops/venues/rs-areas/RA001-Row/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW002-Row", "Row-A", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_ROW_NAME_DUPLICATE"));
    }

    @Test
    void area_row_and_seat_upsert_updates_geometry_and_positions() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V-Layout", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M-LAYOUT", "Layout", 100, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ops/venues/manifests/M-LAYOUT/ga-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGAAreaRequest("GA-1", "L1", "P1", 120, 10, 20, 300, 140))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.x").value(10))
                .andExpect(jsonPath("$.data.width").value(300));

        mockMvc.perform(post("/api/ops/venues/manifests/M-LAYOUT/ga-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGAAreaRequest("GA-1", "L1", "P1", 150, 30, 40, 360, 180))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.x").value(30))
                .andExpect(jsonPath("$.data.height").value(180));

        mockMvc.perform(post("/api/ops/venues/manifests/M-LAYOUT/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRSAreaRequest("RS-1", "L1", 50, 60, 280, 160))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.x").value(50))
                .andExpect(jsonPath("$.data.height").value(160));

        mockMvc.perform(post("/api/ops/venues/manifests/M-LAYOUT/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRSAreaRequest("RS-1", "L1", 80, 90, 340, 200))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.x").value(80))
                .andExpect(jsonPath("$.data.width").value(340));

        mockMvc.perform(post("/api/ops/venues/rs-areas/RS-1/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW-1", "A", 10))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.positionY").value(10));

        mockMvc.perform(post("/api/ops/venues/rs-areas/RS-1/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW-1", "A", 20))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.positionY").value(20));

        mockMvc.perform(post("/api/ops/venues/rows/ROW-1/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("SEAT-1", "1", 0, null, true, false, false, null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.positionX").value(0));

        mockMvc.perform(post("/api/ops/venues/rows/ROW-1/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("SEAT-1", "1", 12, null, false, true, true, io.qzz.hoangvu.ticketpeak.api.venue.model.SeatStatus.UNAVAILABLE, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.positionX").value(12))
                .andExpect(jsonPath("$.data.aisle").value(true))
                .andExpect(jsonPath("$.data.status").value("UNAVAILABLE"));
    }

    @Test
    void manifest_clone_copies_hierarchy() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M006", "Original", 100, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/ops/venues/manifests/M006/levels").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Level 1"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/ops/venues/manifests/M006/clone").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CloneManifestRequest("M006-CLONE", "Clone Layout"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("M006-CLONE"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(get("/api/ops/venues/manifests/M006-CLONE/levels").header("Authorization", "Bearer " + adminToken))
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

        mockMvc.perform(post("/api/ops/venues")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void manifest_update_and_objects_persistence() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V-Obj", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M-OBJ", "Original", 100, null))))
                .andExpect(status().isCreated());

        var objects = java.util.List.of(
                java.util.Map.<String, Object>of("type", "stage", "x", 100, "y", 200, "width", 400, "height", 150),
                java.util.Map.<String, Object>of("type", "label", "text", "Mixer", "x", 500, "y", 600)
        );

        mockMvc.perform(put("/api/ops/venues/manifests/M-OBJ").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateManifestRequest("Updated Layout", 2000, objects))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated Layout"))
                .andExpect(jsonPath("$.data.totalCapacity").value(2000))
                .andExpect(jsonPath("$.data.objects[0].type").value("stage"))
                .andExpect(jsonPath("$.data.objects[0].width").value(400))
                .andExpect(jsonPath("$.data.objects[1].text").value("Mixer"));

        // Test cloning copies objects
        mockMvc.perform(post("/api/ops/venues/manifests/M-OBJ/clone").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CloneManifestRequest("M-OBJ-CLONE", "Clone Layout"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.objects[0].type").value("stage"))
                .andExpect(jsonPath("$.data.objects[1].text").value("Mixer"));
    }

    @Test
    void nullable_ga_area_price_level() throws Exception {
        String responseStr = mockMvc.perform(post("/api/ops/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V-Nullable", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String venueId = JsonPath.read(responseStr, "$.data.id");

        mockMvc.perform(post("/api/ops/venues/" + venueId + "/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M-NULLABLE", "Original", 100, null))))
                .andExpect(status().isCreated());

        // Upsert level (required)
        mockMvc.perform(put("/api/ops/venues/manifests/M-NULLABLE/levels").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Level 1", "#FF0000"))))
                .andExpect(status().isOk());

        // Create GAArea with null/blank priceLevelId
        mockMvc.perform(post("/api/ops/venues/manifests/M-NULLABLE/ga-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGAAreaRequest("GA-1", "L1", "", 150, 30, 40, 360, 180))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.priceLevelId").value(org.hamcrest.Matchers.nullValue()));

        // Create RSArea with geometry only
        mockMvc.perform(post("/api/ops/venues/manifests/M-NULLABLE/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRSAreaRequest("RS-1", "L1", 50, 60, 280, 160))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.x").value(50))
                .andExpect(jsonPath("$.data.width").value(280));
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
