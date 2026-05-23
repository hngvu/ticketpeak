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
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.GAHoldRequest;
import io.qzz.hoangvu.ticketpeak.api.inventory.dto.HoldInventoryRequest;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.GAInventory;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryHoldPlace;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.GAInventoryRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryHoldPlaceRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryCleanupService;
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
    InventoryHoldPlaceRepository inventoryHoldPlaceRepository;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    InventoryCleanupService inventoryCleanupService;

    Account organizerAccount;
    Account buyerAccount;
    String organizerToken;
    String buyerToken;
    Organization organization;
    Venue venue;
    Manifest manifest;

    @BeforeEach
    void setup() throws Exception {
        inventoryHoldPlaceRepository.deleteAll();
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

        buyerAccount = accountRepository.saveAndFlush(Account.builder()
                .email("buyer@ticketpeak.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        organizerToken = login(organizerAccount.getEmail(), rawPassword);
        buyerToken = login(buyerAccount.getEmail(), rawPassword);

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
    void testInventoryInitializationAndHoldsFlow() throws Exception {
        // 1. Add GA Area to layout
        GAArea gaArea = gaAreaRepository.saveAndFlush(GAArea.builder()
                .id("GA-A")
                .manifestId(manifest.getId())
                .levelId("LV-1")
                .sectionId("SEC-A")
                .priceLevelId("PL-1")
                .capacity(50)
                .build());

        // 2. Create and publish event
        Event event = saveEvent("show-init", "Awesome Show", EventStatus.DRAFT);
        
        // Publish event (clones manifest to event snap)
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        // Verify Event is now PUBLISHED and event snap manifest is created
        Event publishedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        
        EventManifest eventManifest = eventManifestRepository.findById(event.getId()).orElseThrow();
        assertThat(eventManifest.getManifestId()).startsWith("evt-");
        String gaAreaId = eventManifest.getManifestId() + "-GA-A";

        // Verify ga_inventory is NOT initialized yet since event is not ONSALE
        assertThat(gaInventoryRepository.existsByEventId(event.getId())).isFalse();

        // 3. Transition event to ONSALE
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/onsale")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        // Verify Event is now ONSALE
        Event onsaleEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(onsaleEvent.getStatus()).isEqualTo(EventStatus.ONSALE);

        // Verify ga_inventory is populated atomically by the EventListener!
        assertThat(gaInventoryRepository.existsByEventId(event.getId())).isTrue();
        GAInventory gaInventory = gaInventoryRepository.findByEventIdAndAreaId(event.getId(), gaAreaId).orElseThrow();
        assertThat(gaInventory.getCapacity()).isEqualTo(50);
        assertThat(gaInventory.getHeld()).isEqualTo(0);
        
        // 4. Test GA Holds and capacity limits
        HoldInventoryRequest gaHoldReq = new HoldInventoryRequest(
                event.getId(),
                List.of(),
                List.of(new GAHoldRequest(gaAreaId, 45)),
                null
        );

        // Hold 45/50 slots (Success)
        String holdResponseStr = mockMvc.perform(post("/api/inventory/hold")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gaHoldReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.holdToken").isNotEmpty())
                .andExpect(jsonPath("$.data.heldGAHolds.length()").value(1))
                .andExpect(jsonPath("$.data.heldGAHolds[0].areaId").value(gaAreaId))
                .andExpect(jsonPath("$.data.heldGAHolds[0].quantity").value(45))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String holdToken = objectMapper.readTree(holdResponseStr).path("data").path("holdToken").asText();

        // Query Availability -> Held = 45, Available = 5
        mockMvc.perform(get("/api/inventory/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory.length()").value(1))
                .andExpect(jsonPath("$.data.gaInventory[0].areaId").value(gaAreaId))
                .andExpect(jsonPath("$.data.gaInventory[0].capacity").value(50))
                .andExpect(jsonPath("$.data.gaInventory[0].held").value(45))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(5));

        // Try to hold another 10 slots (Fails with 409 Conflict due to insufficient capacity)
        HoldInventoryRequest gaHoldOverCapReq = new HoldInventoryRequest(
                event.getId(),
                List.of(),
                List.of(new GAHoldRequest(gaAreaId, 10)),
                null
        );

        mockMvc.perform(post("/api/inventory/hold")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gaHoldOverCapReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_GA_CAPACITY"));
    }

    @Test
    void testReservedSeatingHoldIsolation() throws Exception {
        // 1. Setup Reserved Seating layout
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

        Seat seat = seatRepository.saveAndFlush(Seat.builder()
                .id("SEAT-1")
                .seatRow(seatRow)
                .name("A-01")
                .positionX(1)
                .status(SeatStatus.AVAILABLE)
                .build());

        // 2. Create Event, Publish, and start sales
        Event event = saveEvent("show-rs", "Reserved Show", EventStatus.DRAFT);
        
        // Publish
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        // Get snapshot seat ID in cloned manifest
        EventManifest eventManifest = eventManifestRepository.findById(event.getId()).orElseThrow();
        String snapshotManifestId = eventManifest.getManifestId();
        
        List<Seat> clonedSeats = seatRepository.findByManifestId(snapshotManifestId);
        assertThat(clonedSeats).isNotEmpty();
        String clonedSeatId = clonedSeats.get(0).getId();

        // Start sales
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/onsale")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        // 3. Request hold on this reserved seat
        HoldInventoryRequest holdReq = new HoldInventoryRequest(
                event.getId(),
                List.of(clonedSeatId),
                List.of(),
                null
        );

        mockMvc.perform(post("/api/inventory/hold")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.heldSeatIds[0]").value(clonedSeatId));

        // Verify seat availability is HELD
        mockMvc.perform(get("/api/inventory/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservedSeats.length()").value(1))
                .andExpect(jsonPath("$.data.reservedSeats[0].seatId").value(clonedSeatId))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("HELD"));

        // 4. Try double booking concurrently (Fails with 409 Conflict)
        mockMvc.perform(post("/api/inventory/hold")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_ALREADY_HELD"));
    }

    @Test
    void testLazyCleanupAndWorkerCleanup() throws Exception {
        // 1. Add GA Area to layout
        GAArea gaArea = gaAreaRepository.saveAndFlush(GAArea.builder()
                .id("GA-A")
                .manifestId(manifest.getId())
                .levelId("LV-1")
                .sectionId("SEC-A")
                .priceLevelId("PL-1")
                .capacity(10)
                .build());

        // 2. Create Event, Publish, and start sales
        Event event = saveEvent("show-cleanup", "Cleanup Show", EventStatus.DRAFT);
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/onsale")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        EventManifest eventManifest = eventManifestRepository.findById(event.getId()).orElseThrow();
        String snapshotManifestId = eventManifest.getManifestId();
        String gaAreaId = snapshotManifestId + "-GA-A";

        // 3. Register holds directly using repository to inject an EXPIRED hold
        gaInventoryRepository.holdGAInventory(event.getId(), gaAreaId, 4);
        
        InventoryHoldPlace expiredHold = InventoryHoldPlace.builder()
                .eventId(event.getId())
                .areaId(gaAreaId)
                .quantity(4)
                .holdToken("expired-token")
                .expiresAt(Instant.now().minusSeconds(60)) // Expired 1 minute ago!
                .build();
        inventoryHoldPlaceRepository.saveAndFlush(expiredHold);

        // 4. Test lazy cleanup on availability queries:
        // Expiries are ignored in availability counts!
        mockMvc.perform(get("/api/inventory/event/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory[0].held").value(0)) // Expired hold not counted
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(10)); // Full capacity available

        // 5. Test Background worker cleanup execution
        inventoryCleanupService.cleanupExpiredHolds();

        // Verify the hold record is deleted
        assertThat(inventoryHoldPlaceRepository.findByHoldToken("expired-token")).isEmpty();

        // Verify the held inventory count in ga_inventory is atomically restored!
        GAInventory gaInventory = gaInventoryRepository.findByEventIdAndAreaId(event.getId(), gaAreaId).orElseThrow();
        assertThat(gaInventory.getHeld()).isEqualTo(0);
    }
}
