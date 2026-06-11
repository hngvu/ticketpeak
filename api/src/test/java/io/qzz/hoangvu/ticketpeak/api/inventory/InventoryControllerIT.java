package io.qzz.hoangvu.ticketpeak.api.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventManifest;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventManifestRepository;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventorySeat;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.SeatInventoryStatus;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryGaRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventorySeatRepository;
import io.qzz.hoangvu.ticketpeak.api.inventory.service.InventoryService;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    PriceLevelRepository priceLevelRepository;

    @Autowired
    SectionRepository sectionRepository;

    

    @Autowired
    SeatRowRepository seatRowRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    EventManifestRepository eventManifestRepository;

    @Autowired
    InventoryGaRepository inventoryGaRepository;

    @Autowired
    InventorySeatRepository inventorySeatRepository;

    @Autowired
    OfferRepository offerRepository;

    @Autowired
    TicketTypeRepository ticketTypeRepository;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    EntityManager entityManager;

    Account organizerAccount;
    String organizerToken;
    Organization organization;
    Venue venue;
    Manifest manifest;

    @BeforeEach
    void setup() throws Exception {
        inventorySeatRepository.deleteAll();
        inventoryGaRepository.deleteAll();
        offerRepository.deleteAll();
        eventManifestRepository.deleteAll();
        eventRepository.deleteAll();
        seatRepository.deleteAll();
        seatRowRepository.deleteAll();
        sectionRepository.deleteAll();
        sectionRepository.deleteAll();
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
                
                .totalCapacity(100)
                .status(ManifestStatus.PUBLISHED)
                .build());

        levelRepository.saveAndFlush(Level.builder().id("LV-1").manifest(manifest).build());
        sectionRepository.saveAndFlush(Section.builder().id("SEC-A").manifest(manifest).build());
        priceLevelRepository.saveAndFlush(PriceLevel.builder().id("PL-1").manifest(manifest).build());
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
        sectionRepository.saveAndFlush(Section.builder().type(SectionType.GA)
                .id("GA-A")
                .id(manifest.getId())
                .levelId("LV-1")
                
                .capacity(50)
                .build());

        // 2. Setup Reserved Seating layout
        Section rsArea = sectionRepository.saveAndFlush(Section.builder().type(SectionType.RS)
                .id("RS-A")
                .id(manifest.getId())
                .levelId("LV-1")
                .build());

        SeatRow seatRow = seatRowRepository.saveAndFlush(SeatRow.builder()
                .id("ROW-1")
                .section(rsArea)
                .name("A")
                
                .build());

        seatRepository.saveAndFlush(Seat.builder()
                .id("SEAT-1")
                .seatRow(seatRow)
                .name("A-01")
                .positionX(1)
                
                .sectionId("SEC-A")
                
                .status(SeatStatus.AVAILABLE)
                .build());

        // 3. Create Event
        Event event = saveEvent("show-init", "Awesome Show", EventStatus.DRAFT);

        // 4. Create GA and RS Offers mapped to the Section & Price Level
        Offer gaOffer = offerRepository.saveAndFlush(Offer.builder()
                .eventId(event.getId())
                .ticketTypeId(java.util.UUID.randomUUID())
                .name("Standard GA")
                .currency("VND")
                .faceValue(new BigDecimal("150000.00"))
                .restrictedPayment(false)
                .eventTicketMinimum(1)
                .capacityCap(50)
                .sellableQuantities(List.of(1, 2, 4))
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .sectionId(null)
                
                .charges(List.of())
                .build());

        Offer rsOffer = offerRepository.saveAndFlush(Offer.builder()
                .eventId(event.getId())
                .ticketTypeId(java.util.UUID.randomUUID())
                .name("Reserved Seating")
                .currency("VND")
                .faceValue(new BigDecimal("250000.00"))
                .restrictedPayment(false)
                .eventTicketMinimum(1)
                .capacityCap(10)
                .sellableQuantities(List.of(1))
                .seatingMode(SeatingMode.RESERVED_SEATING)
                .sectionId("SEC-A")
                
                .charges(List.of())
                .build());
        
        // 5. Publish event (creates manifest snap)
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        EventManifest eventManifest = eventManifestRepository.findById(event.getId()).orElseThrow();
        String snapshotManifestId = eventManifest.getManifestId();
        String gaAreaId = snapshotManifestId + "-GA-A";

        List<Seat> clonedSeats = seatRepository.findByManifestId(snapshotManifestId);
        assertThat(clonedSeats).isNotEmpty();
        String clonedSeatId = clonedSeats.get(0).getId();

        // 6. Transition event to ONSALE
        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/onsale")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        // 7. Verify composite read models are initialized cleanly!
        // Verify GA
        assertThat(inventoryGaRepository.existsByEventId(event.getId())).isTrue();
        InventoryGa gaInventory = inventoryGaRepository.findByEventIdAndSectionIdAndOfferId(event.getId(), gaAreaId, gaOffer.getId()).orElseThrow();
        assertThat(gaInventory.getTotal()).isEqualTo(50);
        assertThat(gaInventory.getSold()).isEqualTo(0);
        assertThat(gaInventory.getAvailable()).isEqualTo(50);

        // Verify Seat Status (using composite PK!)
        assertThat(inventorySeatRepository.existsByEventId(event.getId())).isTrue();
        InventorySeat inventorySeat = inventorySeatRepository.findByEventIdAndSeatId(event.getId(), clonedSeatId).orElseThrow();
        assertThat(inventorySeat.getStatus()).isEqualTo(SeatInventoryStatus.AVAILABLE);
        assertThat(inventorySeat.getOfferId()).isEqualTo(rsOffer.getId());

        // 8. Verify GET Availability read path returns Ticketmaster pattern results
        mockMvc.perform(get("/api/events/" + event.getId() + "/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory.length()").value(1))
                .andExpect(jsonPath("$.data.gaInventory[0].areaId").value(gaAreaId))
                .andExpect(jsonPath("$.data.gaInventory[0].total").value(50))
                .andExpect(jsonPath("$.data.gaInventory[0].sold").value(0))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(50))
                .andExpect(jsonPath("$.data.reservedSeats.length()").value(1))
                .andExpect(jsonPath("$.data.reservedSeats[0].seatId").value(clonedSeatId))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("AVAILABLE"));

        // 9. Test Service-layer Holds and Sales Updates
        // GA Hold
        inventoryService.holdGAInventory(event.getId(), gaAreaId, gaOffer.getId(), 5);
        // Reserved Seat Hold
        inventoryService.holdSeat(event.getId(), clonedSeatId);

        // Query Availability -> verify HELD statuses
        mockMvc.perform(get("/api/events/" + event.getId() + "/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory[0].held").value(5))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(45))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("HELD"));

        // Sell
        inventoryService.releaseGAInventory(event.getId(), gaAreaId, gaOffer.getId(), 5); // release holds
        inventoryService.releaseSeat(event.getId(), clonedSeatId);
        
        // 1. Direct Sell GA (without hold)
        inventoryService.directSellGAInventory(event.getId(), gaAreaId, gaOffer.getId(), 10);
        
        // 2. Hold then Confirm GA
        inventoryService.holdGAInventory(event.getId(), gaAreaId, gaOffer.getId(), 5);
        inventoryService.confirmGAInventory(event.getId(), gaAreaId, gaOffer.getId(), 5);
        
        // Sell seat
        inventoryService.sellSeat(event.getId(), clonedSeatId);
 
        // Query Availability -> verify SOLD statuses
        mockMvc.perform(get("/api/events/" + event.getId() + "/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gaInventory[0].sold").value(15))
                .andExpect(jsonPath("$.data.gaInventory[0].available").value(35))
                .andExpect(jsonPath("$.data.reservedSeats[0].status").value("SOLD"));
    }

    @Test
    void ga_mutations_reject_non_positive_quantity() {
        Event event = saveEvent("show-negative-qty", "Negative Quantity Show", EventStatus.DRAFT);

        inventoryGaRepository.saveAndFlush(InventoryGa.builder()
                .eventId(event.getId())
                .sectionId("GA-A")
                .offerId(UUID.randomUUID())
                .total(10)
                .available(10)
                .held(0)
                .sold(0)
                .build());

        assertThatThrownBy(() -> inventoryService.holdGAInventory(event.getId(), "GA-A", UUID.randomUUID(), -5))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo("INVALID_QUANTITY");

        InventoryGa reloaded = inventoryGaRepository.findByEventId(event.getId()).get(0);
        assertThat(reloaded.getAvailable()).isEqualTo(10);
        assertThat(reloaded.getHeld()).isEqualTo(0);
        assertThat(reloaded.getSold()).isEqualTo(0);
    }

    @Test
    void onsale_transition_rolls_back_when_reserved_seat_has_no_matching_offer() throws Exception {
        sectionRepository.saveAndFlush(Section.builder().type(SectionType.GA)
                .id("GA-B")
                .id(manifest.getId())
                .levelId("LV-1")
                
                .capacity(10)
                .build());

        Section rsArea = sectionRepository.saveAndFlush(Section.builder().type(SectionType.RS)
                .id("RS-B")
                .id(manifest.getId())
                .levelId("LV-1")
                .build());

        SeatRow seatRow = seatRowRepository.saveAndFlush(SeatRow.builder()
                .id("ROW-B")
                .section(rsArea)
                .name("B")
                
                .build());

        seatRepository.saveAndFlush(Seat.builder()
                .id("SEAT-B")
                .seatRow(seatRow)
                .name("B-01")
                .positionX(1)
                
                .sectionId("SEC-A")
                
                .status(SeatStatus.AVAILABLE)
                .build());

        Event event = saveEvent("show-missing-offer", "Missing Offer Show", EventStatus.DRAFT);

        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/partner/events/" + event.getId() + "/onsale")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_OFFER_MAPPING"));

        entityManager.clear();
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(inventorySeatRepository.existsByEventId(event.getId())).isFalse();
        assertThat(inventoryGaRepository.existsByEventId(event.getId())).isFalse();
    }
}
