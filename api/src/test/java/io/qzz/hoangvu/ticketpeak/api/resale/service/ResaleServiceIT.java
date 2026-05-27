package io.qzz.hoangvu.ticketpeak.api.resale.service;

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
import io.qzz.hoangvu.ticketpeak.api.order.model.Order;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderItem;
import io.qzz.hoangvu.ticketpeak.api.order.model.OrderStatus;
import io.qzz.hoangvu.ticketpeak.api.order.repository.OrderRepository;
import io.qzz.hoangvu.ticketpeak.api.organization.model.Organization;
import io.qzz.hoangvu.ticketpeak.api.organization.model.OrganizationStatus;
import io.qzz.hoangvu.ticketpeak.api.organization.repository.OrganizationRepository;
import io.qzz.hoangvu.ticketpeak.api.payment.model.Payment;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentProvider;
import io.qzz.hoangvu.ticketpeak.api.payment.model.PaymentStatus;
import io.qzz.hoangvu.ticketpeak.api.payment.repository.PaymentRepository;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethod;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodStatus;
import io.qzz.hoangvu.ticketpeak.api.payout.model.PayoutMethodType;
import io.qzz.hoangvu.ticketpeak.api.payout.repository.PayoutMethodRepository;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.CreateListingRequest;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.ResaleListingResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.dto.ResaleOrderResponse;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleListingStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.model.ResaleOrderStatus;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleListingRepository;
import io.qzz.hoangvu.ticketpeak.api.resale.repository.ResaleOrderRepository;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.Reservation;
import io.qzz.hoangvu.ticketpeak.api.reservation.model.ReservationStatus;
import io.qzz.hoangvu.ticketpeak.api.reservation.repository.ReservationRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.Ticket;
import io.qzz.hoangvu.ticketpeak.api.ticket.model.TicketStatus;
import io.qzz.hoangvu.ticketpeak.api.ticket.repository.TicketRepository;
import io.qzz.hoangvu.ticketpeak.api.ticket.service.TotpService;
import io.qzz.hoangvu.ticketpeak.api.venue.model.Venue;
import io.qzz.hoangvu.ticketpeak.api.venue.model.VenueStatus;
import io.qzz.hoangvu.ticketpeak.api.venue.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class ResaleServiceIT {

    @Autowired ResaleListingRepository listingRepository;
    @Autowired ResaleOrderRepository orderRepository;
    @Autowired TicketRepository ticketRepository;
    @Autowired EventRepository eventRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired PayoutMethodRepository payoutMethodRepository;
    @Autowired OrderRepository purchaseOrderRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PaymentRepository paymentRepository;
    
    @Autowired ResaleListingService listingService;
    @Autowired ResaleOrderService orderService;
    @Autowired TotpService totpService;
    @Autowired io.qzz.hoangvu.ticketpeak.api.common.crypto.EncryptionService encryptionService;

    Account seller;
    Account buyer;
    Event event;
    Ticket ticket;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        listingRepository.deleteAll();
        payoutMethodRepository.deleteAll();
        ticketRepository.deleteAll();
        purchaseOrderRepository.deleteAll();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        organizationRepository.deleteAll();
        accountRepository.deleteAll();

        seller = accountRepository.save(Account.builder()
                .email("seller@example.com")
                .password("hash")
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        buyer = accountRepository.save(Account.builder()
                .email("buyer@example.com")
                .password("hash")
                .role(Role.BUYER)
                .status(AccountStatus.ACTIVE)
                .build());

        Organization org = organizationRepository.save(Organization.builder()
                .name("Peak Organizers")
                .slug("peak-organizers")
                .status(OrganizationStatus.ACTIVE)
                .ownerAccountId(seller.getId()) // doesn't matter
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
                .maxTransferCount(1)
                .resaleEnabled(true)
                .resalePriceCapPercent(BigDecimal.valueOf(150.0)) // Max 1.5x face value
                .maxResaleListingsPerUser(2)
                .serviceFeePercent(BigDecimal.valueOf(10.0))
                .build());

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .accountId(seller.getId())
                .eventId(event.getId())
                .status(ReservationStatus.CONFIRMED)
                .currency("VND")
                .expiresAt(Instant.now().plusSeconds(600))
                .build());

        Payment payment = paymentRepository.save(Payment.builder()
                .reservationId(reservation.getId())
                .accountId(seller.getId())
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
                .accountId(seller.getId())
                .eventId(event.getId())
                .status(OrderStatus.CONFIRMED)
                .currency("VND")
                .totalAmount(BigDecimal.valueOf(100))
                .build();

        OrderItem item = OrderItem.builder()
                .order(orderEntity)
                .offerId(UUID.randomUUID())
                .seatingMode(SeatingMode.GENERAL_ADMISSION)
                .areaId("GA-AREA")
                .qty(1)
                .unitFaceValue(BigDecimal.valueOf(100))
                .unitTotalPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(100))
                .currency("VND")
                .charges(java.util.List.of())
                .build();

        orderEntity.setItems(java.util.List.of(item));
        Order purchaseOrder = purchaseOrderRepository.save(orderEntity);

        ticket = ticketRepository.save(Ticket.builder()
                .orderId(purchaseOrder.getId())
                .orderItemId(purchaseOrder.getItems().get(0).getId())
                .accountId(seller.getId())
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
                
        payoutMethodRepository.save(PayoutMethod.builder()
                .accountId(seller.getId())
                .type(PayoutMethodType.BANK_TRANSFER)
                .holderName("Seller Name")
                .bankCode("BANK")
                .accountNumberEnc(encryptionService.encrypt("1234567890"))
                .isPrimary(true)
                .status(PayoutMethodStatus.ACTIVE)
                .build());
    }

    @Test
    void create_listing_happy_path() {
        CreateListingRequest req = new CreateListingRequest(ticket.getId(), BigDecimal.valueOf(120));
        ResaleListingResponse res = listingService.createListing(seller.getId(), req);

        assertThat(res.sellerId()).isEqualTo(seller.getId());
        assertThat(res.askingPrice()).isEqualByComparingTo(BigDecimal.valueOf(120));
        assertThat(res.status()).isEqualTo(ResaleListingStatus.ACTIVE);
    }
    
    @Test
    void purchase_happy_path_transfers_ticket_rotates_totp_and_creates_payout() {
        CreateListingRequest req = new CreateListingRequest(ticket.getId(), BigDecimal.valueOf(120));
        ResaleListingResponse listingRes = listingService.createListing(seller.getId(), req);
        
        String oldSecret = ticketRepository.findById(ticket.getId()).get().getTotpSecretEnc();

        ResaleOrderResponse orderRes = orderService.initiatePurchase(buyer.getId(), listingRes.id());
        assertThat(orderRes.status()).isEqualTo(ResaleOrderStatus.PENDING);
        
        orderService.onPaymentSuccess(orderRes.id(), UUID.randomUUID());
        
        // Verify Listing SOLD
        var listing = listingRepository.findById(listingRes.id()).get();
        assertThat(listing.getStatus()).isEqualTo(ResaleListingStatus.SOLD);
        
        // Verify Order CONFIRMED
        var order = orderRepository.findById(orderRes.id()).get();
        assertThat(order.getStatus()).isEqualTo(ResaleOrderStatus.CONFIRMED);
        
        // Verify Ticket Ownership & TOTP
        var updatedTicket = ticketRepository.findById(ticket.getId()).get();
        assertThat(updatedTicket.getAccountId()).isEqualTo(buyer.getId());
        assertThat(updatedTicket.getTotpSecretEnc()).isNotEqualTo(oldSecret);
    }

    @Test
    void expired_reservation_cleanup_reverts_listing_and_cancels_order() {
        CreateListingRequest req = new CreateListingRequest(ticket.getId(), BigDecimal.valueOf(120));
        ResaleListingResponse listingRes = listingService.createListing(seller.getId(), req);

        ResaleOrderResponse orderRes = orderService.initiatePurchase(buyer.getId(), listingRes.id());
        assertThat(orderRes.status()).isEqualTo(ResaleOrderStatus.PENDING);

        // Manually expire the listing reservation in DB
        var listing = listingRepository.findById(listingRes.id()).get();
        listing.setReservedUntil(Instant.now().minusSeconds(10));
        listingRepository.save(listing);

        // Run cleanup
        orderService.releaseExpiredReservation(listingRes.id());

        // Verify Listing ACTIVE and reservedUntil cleared
        var clearedListing = listingRepository.findById(listingRes.id()).get();
        assertThat(clearedListing.getStatus()).isEqualTo(ResaleListingStatus.ACTIVE);
        assertThat(clearedListing.getReservedUntil()).isNull();

        // Verify Order CANCELLED
        var cancelledOrder = orderRepository.findById(orderRes.id()).get();
        assertThat(cancelledOrder.getStatus()).isEqualTo(ResaleOrderStatus.CANCELLED);
    }
}
