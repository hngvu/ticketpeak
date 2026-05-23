package io.qzz.hoangvu.ticketpeak.api.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventManifest;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.GAInventory;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryControllerIT {

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
    GAAreaRepository gaAreaRepository;

    @Autowired
    RSAreaRepository rsAreaRepository;

    @Autowired
    SeatRowRepository seatRowRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    EventManifestRepository eventManifestRepository;

    @Autowired
    GAInventoryRepository gaInventoryRepository;

    @Autowired
    InventorySeatRepository inventorySeatRepository;

    @Autowired
    InventoryService inventoryService;

    Account organizerAccount;
    String organizerToken;
    Organization organization;
    Venue venue;
    Manifest manifest;

    @BeforeEach
    void setup() throws Exception {
        inventorySeatRepository.deleteAll();
        gaInventoryRepository.deleteAll();
        eventManifestRepository.deleteAll();
        eventRepository.deleteAll();
        seatRepository.deleteAll();
        seatRowRepository.deleteAll();
        rsAreaRepository.deleteAll();
        gaAreaRepository.deleteAll();
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
                .role(Role.ORGANIZER)
                .status(AccountStatus.ACTIVE)
                .build());

        organizerToken = login(organizerAccount.getEmail(), rawPassword);

        organization = organizationRepository.saveAndFlush(Organization.builder()
                .name("Inventory Org")
                .slug("inventory-org")
                .ownerAccountId(organizerAccount.getId())
                .status(OrganizationStatus.ACTIVE)
                .build());

        venue = venueRepository.saveAndFlush(Venue.builder()
                .name("Inventory Arena")
                .address("123 Arena Street")
                .city("Hanoi")
                .country("Vietnam")
                .status(VenueStatus.ACTIVE)
                .images(List.of())
                .build());

        manifest = manifestRepository.saveAndFlush(Manifest.builder()
                .id("M-INVENTORY-1")
                .venue(venue)
                .description("Inventory Manifest")
                .totalCapacity(100)
                .status(ManifestStatus.PUBLISHED)
                .build());

        levelRepository.saveAndFlush(Level.builder().id("LV-1").manifest(manifest).description("Level 1").build());
        sectionRepository.saveAndFlush(Section.builder().id("SEC-A").manifest(manifest).description("Section A").build());
        priceLevelRepository.saveAndFlush(PriceLevel.builder().id("PL-1").manifest(manifest).description("Price Level 1").build());
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

    private record LoginPayload(String email, String password) {}

    @Test
    void testInventoryInitializationAndAvailabilityFlow() throws Exception {
        // 1. Add GA Area to layout
        gaAreaRepository.saveAndFlush(GAArea.builder()
                .id("GA-A")
                .manifestId(manifest.getId())
                .levelId("LV-1")
                .sectionId("SEC-A")
                .priceLevelId("PL-1")
                .capacity(50)
                .build());

        // 2. Setup Reserved Seating layout
        RSArea rsArea = rsAreaRepository.saveAndFlush(RSArea.builder()
                .id("RS-A")
                .manifestId(manifest.getId())
                .levelId("LV-1")
                .sectionId("SEC-A")
                .priceLevelId("PL-1")
                .build());

        SeatRow seatRow = seatRowRepository.saveAndFlush(SeatRow.builder()
                .id("ROW-1")
                .rsArea(rsArea)
                .name("A")
                .positionY(1)
                .build());

        seatRepository.saveAndFlush(Seat.builder()
                .id("SEAT-1")
                .seatRow(seatRow)
                .name("A-01")
                .positionX(1)
                .status(SeatStatus.AVAILABLE)
                .build());

        // 3. Create and publish event
        Event event = saveEvent("show-init", "Awesome Show", EventStatus.DRAFT);
        
        // Publish event
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        EventManifest eventManifest = eventManifestRepository.findById(event.getId()).orElseThrow();
        String snapshotManifestId = eventManifest.getManifestId();
        String gaAreaId = snapshotManifestId + "-GA-A";

        List<Seat> clonedSeats = seatRepository.findByManifestId(snapshotManifestId);
        assertThat(clonedSeats).isNotEmpty();
        String clonedSeatId = clonedSeats.get(0).getId();

        // 4. Transition event to ONSALE
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/onsale")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        // 5. Verify read models are initialized in bulk!
        // Verify GA
        assertThat(gaInventoryRepository.existsByEventId(event.getId())).isTrue();
        GAInventory gaInventory = gaInventoryRepository.findByEventIdAndAreaId(event.getId(), gaAreaId).orElseThrow();
        assertThat(gaInventory.getCapacity()).isEqualTo(50);
        assertThat(gaInventory.getSold()).isEqualTo(0);

        // Verify Seat Status
        assertThat(inventorySeatRepository.existsByEventId(event.getId())).isTrue();
        InventorySeat inventorySeat = inventorySeatRepository.findByEventIdAndSeatId(event.getId(), clonedSeatId).orElseThrow();
        assertThat(inventorySeat.getStatus()).isEqualTo("AVAILABLE");

        // 6. Verify GET Availability read path returns exact counts
        mockMvc.perform(get("/api/inventory/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory.length()").value(1))
                .andExpect(jsonPath("$.data.gaInventory[0].areaId").value(gaAreaId))
                .andExpect(jsonPath("$.data.gaInventory[0].capacity").value(50))
                .andExpect(jsonPath("$.data.gaInventory[0].sold").value(0))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(50))
                .andExpect(jsonPath("$.data.reservedSeats.length()").value(1))
                .andExpect(jsonPath("$.data.reservedSeats[0].seatId").value(clonedSeatId))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("AVAILABLE"));

        // 7. Verify Sales Updates
        inventoryService.sellGAInventory(event.getId(), gaAreaId, 10);
        inventoryService.sellSeat(event.getId(), clonedSeatId);

        // Query Availability again -> reflect sold items
        mockMvc.perform(get("/api/inventory/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory[0].sold").value(10))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(40))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("SOLD"));

        // 8. Verify Refunds
        inventoryService.refundGAInventory(event.getId(), gaAreaId, 5);
        inventoryService.refundSeat(event.getId(), clonedSeatId);

        // Query Availability -> restored
        mockMvc.perform(get("/api/inventory/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory[0].sold").value(5))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(45))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("AVAILABLE"));
    }
}
