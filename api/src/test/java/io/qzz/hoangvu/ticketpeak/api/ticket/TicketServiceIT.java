package io.qzz.hoangvu.ticketpeak.api.ticket;

import io.qzz.hoangvu.ticketpeak.api.TestcontainersConfiguration;
import io.qzz.hoangvu.ticketpeak.api.account.model.Account;
import io.qzz.hoangvu.ticketpeak.api.account.model.AccountStatus;
import io.qzz.hoangvu.ticketpeak.api.account.repository.AccountRepository;
import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.order.model.Order;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderItem;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderStatus;
import io.qzz.hoangvu.ticketpeak.api.order.repository.OrderRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.InitiateTransferRequest;
import io.qzz.hoangvu.ticketpeak.api.ticket.dto.TransferResponse;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransfer;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketTransferStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketTransferRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.service.TicketService;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class TicketServiceIT {

    @Autowired TicketTransferRepository transferRepository;
    @Autowired TicketRepository ticketRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired EventRepository eventRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired TicketService ticketService;

    Account sender;
    Account recipient;
    Account outsider;
    Event event;
    Order order;
    OrderItem orderItem;

    @BeforeEach
    void setup() {
        transferRepository.deleteAll();
        ticketRepository.deleteAll();
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        organizationRepository.deleteAll();
        accountRepository.deleteAll();

        sender = accountRepository.save(Account.builder()
                .email("sender@example.com")
                .password("hash")
                .roles(java.util.Set.of(Role.BUYER))
                .status(AccountStatus.ACTIVE)
                .build());

        recipient = accountRepository.save(Account.builder()
                .email("recipient@example.com")
                .password("hash")
                .roles(java.util.Set.of(Role.BUYER))
                .status(AccountStatus.ACTIVE)
                .build());

        outsider = accountRepository.save(Account.builder()
                .email("outsider@example.com")
                .password("hash")
                .roles(java.util.Set.of(Role.BUYER))
                .status(AccountStatus.ACTIVE)
                .build());

        Organization org = organizationRepository.save(Organization.builder()
                .name("Peak Organizers")
                .slug("peak-organizers")
                .status(OrganizationStatus.ACTIVE)
                .ownerAccountId(sender.getId())
                .build());

        Venue venue = venueRepository.save(Venue.builder()
                .name("Venue")
                .status(VenueStatus.ACTIVE)
                .country("VN")
                .city("HN")
                .address("Address")
                .build());

        event = eventRepository.save(Event.builder()
                .organizationId(org.getId())
                .venueId(venue.getId())
                .title("Event title")
                .slug("event-slug")
                .status(EventStatus.PUBLISHED)
                .startAt(Instant.now().plusSeconds(86400))
                .endAt(Instant.now().plusSeconds(90000))
                .timezone("Asia/Ho_Chi_Minh")
                .transferEnabled(true)
                .maxTransferCount(1) // Max 1 transfer allowed
                .build());

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .accountId(sender.getId())
                .eventId(event.getId())
                .status(ReservationStatus.CONFIRMED)
                .currency("VND")
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        Payment payment = paymentRepository.save(Payment.builder()
                .reservationId(reservation.getId())
                .accountId(sender.getId())
                .eventId(event.getId())
                .provider(PaymentProvider.STRIPE)
                .status(PaymentStatus.COMPLETED)
                .amount(BigDecimal.valueOf(100))
                .currency("VND")
                .gatewayPayload(java.util.Map.of())
                .gatewayResponse(java.util.Map.of())
                .build());

        Order orderEntity = Order.builder()
                .reservationId(reservation.getId())
                .paymentId(payment.getId())
                .accountId(sender.getId())
                .eventId(event.getId())
                .status(OrderStatus.CONFIRMED)
                .currency("VND")
                .totalAmount(BigDecimal.valueOf(100))
                .build();

        OrderItem item = OrderItem.builder()
                .order(orderEntity)
                .offerId(UUID.randomUUID())
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .sectionId("GA-AREA")
                .qty(1)
                .unitFaceValue(BigDecimal.valueOf(100))
                .unitTotalPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(100))
                .currency("VND")
                .charges(List.of())
                .build();

        orderEntity.setItems(List.of(item));
        order = orderRepository.save(orderEntity);
        orderItem = order.getItems().get(0);
    }

    @Test
    void test_transfer_lifecycle_and_limit_check() {
        // Arrange: Create a fresh issued ticket
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .orderId(order.getId())
                .orderItemId(orderItem.getId())
                .accountId(sender.getId())
                .eventId(event.getId())
                .offerId(UUID.randomUUID())
                .ticketTypeId(UUID.randomUUID())
                .ticketTypeName("Standard")
                .offerName("Early Bird")
                .faceValue(BigDecimal.valueOf(100))
                .currency("VND")
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .totpSecretEnc("encrypted")
                .status(TicketStatus.ISSUED)
                .transferCount(0)
                .build());

        // 1. Initiate transfer
        InitiateTransferRequest req = new InitiateTransferRequest(recipient.getId());
        TransferResponse initiateRes = ticketService.initiateTransfer(sender.getId(), ticket.getId(), req);

        assertThat(initiateRes.senderId()).isEqualTo(sender.getId());
        assertThat(initiateRes.recipientId()).isEqualTo(recipient.getId());
        assertThat(initiateRes.status()).isEqualTo("PENDING");

        Ticket ticketPending = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(ticketPending.getStatus()).isEqualTo(TicketStatus.TRANSFERRED);

        // 2. Accept transfer
        ticketService.acceptTransfer(recipient.getId(), initiateRes.transferId());

        Ticket ticketAccepted = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(ticketAccepted.getAccountId()).isEqualTo(recipient.getId());
        assertThat(ticketAccepted.getStatus()).isEqualTo(TicketStatus.ISSUED);
        assertThat(ticketAccepted.getTransferCount()).isEqualTo(1); // Counter incremented!

        // 3. Try to initiate transfer again from recipient to outsider -> exceeds limit (max is 1, current is 1)
        InitiateTransferRequest req2 = new InitiateTransferRequest(outsider.getId());
        assertThatThrownBy(() -> ticketService.initiateTransfer(recipient.getId(), ticket.getId(), req2))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("This ticket has reached the maximum number of transfers");
    }

    @Test
    void test_cancel_transfer_prevents_info_leakage() {
        // Arrange
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .orderId(order.getId())
                .orderItemId(orderItem.getId())
                .accountId(sender.getId())
                .eventId(event.getId())
                .offerId(UUID.randomUUID())
                .ticketTypeId(UUID.randomUUID())
                .ticketTypeName("Standard")
                .offerName("Early Bird")
                .faceValue(BigDecimal.valueOf(100))
                .currency("VND")
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .totpSecretEnc("encrypted")
                .status(TicketStatus.ISSUED)
                .transferCount(0)
                .build());

        InitiateTransferRequest req = new InitiateTransferRequest(recipient.getId());
        TransferResponse initiateRes = ticketService.initiateTransfer(sender.getId(), ticket.getId(), req);

        // Act & Assert 1: Cancel by wrong sender -> returns transferNotFound (not forbidden/unauthorized)
        assertThatThrownBy(() -> ticketService.cancelTransfer(outsider.getId(), initiateRes.transferId()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus().value()).isEqualTo(404);
                    assertThat(apiEx.getErrorCode()).isEqualTo("TICKET_TRANSFER_NOT_FOUND");
                });

        // Act & Assert 2: Cancel successfully by correct sender
        ticketService.cancelTransfer(sender.getId(), initiateRes.transferId());

        TicketTransfer cancelledTransfer = transferRepository.findById(initiateRes.transferId()).orElseThrow();
        assertThat(cancelledTransfer.getStatus()).isEqualTo(TicketTransferStatus.CANCELLED);

        Ticket ticketRestored = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(ticketRestored.getStatus()).isEqualTo(TicketStatus.ISSUED);

        // Act & Assert 3: Cancel already cancelled transfer -> returns transferNotFound (prevents status leak)
        assertThatThrownBy(() -> ticketService.cancelTransfer(sender.getId(), initiateRes.transferId()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus().value()).isEqualTo(404);
                    assertThat(apiEx.getErrorCode()).isEqualTo("TICKET_TRANSFER_NOT_FOUND");
                });
    }

    @Test
    void test_partial_unique_index_prevents_duplicate_pending_transfers() {
        // Arrange
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .orderId(order.getId())
                .orderItemId(orderItem.getId())
                .accountId(sender.getId())
                .eventId(event.getId())
                .offerId(UUID.randomUUID())
                .ticketTypeId(UUID.randomUUID())
                .ticketTypeName("Standard")
                .offerName("Early Bird")
                .faceValue(BigDecimal.valueOf(100))
                .currency("VND")
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .totpSecretEnc("encrypted")
                .status(TicketStatus.ISSUED)
                .transferCount(0)
                .build());

        // 1. Manually create a pending transfer in DB bypass service check
        transferRepository.save(TicketTransfer.builder()
                .ticketId(ticket.getId())
                .senderId(sender.getId())
                .recipientId(recipient.getId())
                .status(TicketTransferStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build());

        // 2. Try to insert another pending transfer directly -> breaks partial index
        TicketTransfer duplicateTransfer = TicketTransfer.builder()
                .ticketId(ticket.getId())
                .senderId(sender.getId())
                .recipientId(outsider.getId())
                .status(TicketTransferStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        assertThatThrownBy(() -> transferRepository.saveAndFlush(duplicateTransfer))
                .isInstanceOf(Exception.class);
    }
}
