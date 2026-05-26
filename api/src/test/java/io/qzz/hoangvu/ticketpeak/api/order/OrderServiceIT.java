package io.qzz.hoangvu.ticketpeak.api.order;

import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.inventory.model.InventoryGa;
import io.qzz.hoangvu.ticketpeak.api.inventory.repository.InventoryGaRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.Offer;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.OfferRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.repository.TicketTypeRepository;
import io.qzz.hoangvu.ticketpeak.api.offer.model.TicketType;
import io.qzz.hoangvu.ticketpeak.api.order.dto.OrderResponse;
import io.qzz.hoangvu.ticketpeak.api.order.model.Order;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderStatus;
import io.qzz.hoangvu.ticketpeak.api.order.repository.OrderRepository;
import io.qzz.hoangvu.ticketpeak.api.order.service.OrderService;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.event.PaymentCompletedEvent;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationItem;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class OrderServiceIT {

    @Autowired OrderRepository orderRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired InventoryGaRepository inventoryGaRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired EventRepository eventRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired OrderService orderService;

    Account buyer;
    Event event;
    Offer offer;
    String areaId = "GA-AREA";

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        inventoryGaRepository.deleteAll();
        offerRepository.deleteAll();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        organizationRepository.deleteAll();
        accountRepository.deleteAll();

        buyer = accountRepository.save(Account.builder()
                .email("buyer@example.com")
                .password("hash")
                .firstName("Buyer").lastName("One")
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        Organization org = organizationRepository.save(Organization.builder()
                .name("Org").slug("org").status(OrganizationStatus.ACTIVE).ownerAccountId(buyer.getId()).build());
        
        Venue venue = venueRepository.save(Venue.builder()
                .name("Venue").status(VenueStatus.ACTIVE)
                .country("US").city("NY").address("Address").build());

        event = eventRepository.save(Event.builder()
                .organizationId(org.getId()).venueId(venue.getId())
                .title("Event").slug("event").status(EventStatus.PUBLISHED).timezone("UTC")
                .startAt(Instant.now().plusSeconds(86400)).endAt(Instant.now().plusSeconds(90000)).build());

        offer = offerRepository.save(Offer.builder()
                .eventId(event.getId()).name("GA").seatingMode(SeatingMode.GENERAL_ADMISSION)
                .ticketTypeId(java.util.UUID.randomUUID()).faceValue(new BigDecimal("100.00"))
                .currency("USD")
                .eventTicketMinimum(1)
                .restrictedPayment(false)
                .capacityCap(500)
                .sellableQuantities(List.of(1, 2, 3))
                .charges(List.of())
                .build());

        inventoryGaRepository.save(InventoryGa.builder()
                .eventId(event.getId()).areaId(areaId).offerId(offer.getId())
                .total(100).available(98).held(2).sold(0).build());
    }

    @Test
    void happy_path_creates_order_and_finalizes_reservation() {
        // Arrange
        Reservation reservation = Reservation.builder()
                .accountId(buyer.getId()).eventId(event.getId())
                .status(ReservationStatus.CONFIRMED).currency("USD")
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        
        ReservationItem item = ReservationItem.builder()
                .reservation(reservation)
                .offerId(offer.getId())
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .areaId(areaId)
                .qty(2)
                .unitFaceValue(BigDecimal.valueOf(50))
                .currency("USD")
                .charges(List.of())
                .build();
        reservation.setItems(List.of(item));
        reservation = reservationRepository.save(reservation);

        Payment payment = paymentRepository.save(Payment.builder()
                .reservationId(reservation.getId())
                .accountId(buyer.getId())
                .eventId(event.getId())
                .provider(PaymentProvider.STRIPE)
                .status(PaymentStatus.COMPLETED)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .gatewayPayload(java.util.Map.of())
                .gatewayResponse(java.util.Map.of())
                .build());

        PaymentCompletedEvent eventPublisher = new PaymentCompletedEvent(
                this, payment.getId(), reservation.getId(), event.getId(), buyer.getId(), BigDecimal.valueOf(100), "USD"
        );

        // Act
        orderService.onPaymentCompleted(eventPublisher);

        // Assert
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        Order order = orders.get(0);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQty()).isEqualTo(2);
        
        Reservation updatedRes = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updatedRes.getStatus()).isEqualTo(ReservationStatus.FINALIZED);

        InventoryGa inventory = inventoryGaRepository.findByEventId(event.getId()).get(0);
        assertThat(inventory.getHeld()).isEqualTo(0);
        assertThat(inventory.getSold()).isEqualTo(2);
    }
}
