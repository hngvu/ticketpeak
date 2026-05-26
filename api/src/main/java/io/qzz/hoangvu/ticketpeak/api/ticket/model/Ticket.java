package io.qzz.hoangvu.ticketpeak.api.ticket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "order_id", nullable = false)
    UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    UUID orderItemId;

    @Column(name = "account_id", nullable = false)
    UUID accountId;          // mutable — changes on transfer

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    // Snapshot fields
    @Column(name = "ticket_type_id", nullable = false)
    UUID ticketTypeId;

    @Column(name = "ticket_type_name", nullable = false)
    String ticketTypeName;

    @Column(name = "offer_name", nullable = false)
    String offerName;

    @Column(name = "face_value", nullable = false)
    BigDecimal faceValue;

    @Column(nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false, length = 32)
    SeatingMode seatingMode;

    @Column(name = "area_id", length = 64)
    String areaId;

    @Column(name = "seat_id", length = 64)
    String seatId;

    @JsonIgnore
    @Column(name = "totp_secret_enc", nullable = false, columnDefinition = "TEXT")
    String totpSecretEnc;    // never returned to client; used only by TotpService

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    TicketStatus status;

    @Column(name = "transfer_count", nullable = false)
    int transferCount;

    @Column(name = "checked_in_at")
    Instant checkedInAt;

    @Version
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
