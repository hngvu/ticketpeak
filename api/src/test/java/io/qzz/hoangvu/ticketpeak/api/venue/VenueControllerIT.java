package io.qzz.hoangvu.ticketpeak.api.venue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.venue.dto.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class VenueControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired ManifestRepository manifestRepository;
    @Autowired LevelRepository levelRepository;
    @Autowired SectionRepository sectionRepository;
    @Autowired PriceLevelRepository priceLevelRepository;
    @Autowired GAAreaRepository gaAreaRepository;
    @Autowired RSAreaRepository rsAreaRepository;
    @Autowired SeatRowRepository seatRowRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired PasswordEncoder passwordEncoder;

    String adminToken;

    @BeforeEach
    void setup() throws Exception {
        seatRepository.deleteAll();
        seatRowRepository.deleteAll();
        rsAreaRepository.deleteAll();
        gaAreaRepository.deleteAll();
        priceLevelRepository.deleteAll();
        sectionRepository.deleteAll();
        levelRepository.deleteAll();
        manifestRepository.deleteAll();
        venueRepository.deleteAll();
        accountRepository.deleteAll();

        String rawPassword = "Password123!";
        Account admin = accountRepository.saveAndFlush(Account.builder()
                .email("admin@tp.com").password(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN).status(AccountStatus.ACTIVE).build());

        adminToken = login("admin@tp.com", rawPassword);
    }

    @Test
    void admin_can_create_and_get_venue() throws Exception {
        CreateVenueRequest req = new CreateVenueRequest(
                "V001", "Saigon Hall", "1 Le Duan", "Ho Chi Minh City", "VN",
                null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("V001"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/admin/venues/V001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Saigon Hall"));
    }

    @Test
    void venue_deactivate_and_activate() throws Exception {
        mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V002", "Arena", "2 Street", "HCM", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/venues/V002/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/admin/venues/V002/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void manifest_lifecycle_at_most_one_published() throws Exception {
        // Create venue + 2 manifests
        mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V003", "Test Venue", "Addr", "City", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/venues/V003/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M001", "Layout A", 1000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(post("/api/admin/venues/V003/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M002", "Layout B", 500))))
                .andExpect(status().isCreated());

        // Publish M001
        mockMvc.perform(post("/api/admin/venues/manifests/M001/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // Publish M002 — should auto-archive M001
        mockMvc.perform(post("/api/admin/venues/manifests/M002/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        assertThat(manifestRepository.findById("M001").orElseThrow().getStatus().name()).isEqualTo("ARCHIVED");
    }

    @Test
    void cannot_publish_non_draft_manifest() throws Exception {
        mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V004", "V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/venues/V004/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M003", "L", 100))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/venues/manifests/M003/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Try to publish again
        mockMvc.perform(post("/api/admin/venues/manifests/M003/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_MANIFEST_STATUS"));
    }

    @Test
    void lookup_tables_upsert_and_list() throws Exception {
        mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V005", "V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/venues/V005/manifests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M004", "L", 100))))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/admin/venues/manifests/M004/levels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Floor 1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Floor 1"));

        mockMvc.perform(get("/api/admin/venues/manifests/M004/levels")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void seat_uniqueness_enforced() throws Exception {
        // Setup venue -> manifest -> rs_area -> row
        mockMvc.perform(post("/api/admin/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V006", "V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/venues/V006/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M005", "L", 100))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/venues/manifests/M005/rs-areas").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRSAreaRequest("RA001", "L1", "S1", "P1"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/venues/rs-areas/RA001/rows").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRowRequest("ROW001", "A", null))))
                .andExpect(status().isCreated());

        // First seat OK
        mockMvc.perform(post("/api/admin/venues/rows/ROW001/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("S001", "1", null, null, null, null))))
                .andExpect(status().isCreated());

        // Duplicate name => conflict
        mockMvc.perform(post("/api/admin/venues/rows/ROW001/seats").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("S002", "1", null, null, null, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_NAME_DUPLICATE"));
    }

    @Test
    void manifest_clone_copies_hierarchy() throws Exception {
        mockMvc.perform(post("/api/admin/venues").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateVenueRequest("V007", "V", "A", "C", "VN", null, null, null, null, null, null, null, null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/venues/V007/manifests").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateManifestRequest("M006", "Original", 100))))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/admin/venues/manifests/M006/levels").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertLookupRequest("L1", "Level 1"))))
                .andExpect(status().isOk());

        // Clone
        mockMvc.perform(post("/api/admin/venues/manifests/M006/clone").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CloneManifestRequest("M006-CLONE", "Clone Layout"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("M006-CLONE"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        // Verify level was cloned
        mockMvc.perform(get("/api/admin/venues/manifests/M006-CLONE/levels").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
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
