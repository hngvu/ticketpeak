package io.qzz.hoangvu.ticketpeak.api.resale.model;

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
@Table(name = "resale_listing")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ResaleListing {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "ticket_id", nullable = false)
    UUID ticketId;

    @Column(name = "seller_id", nullable = false)
    UUID sellerId;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    @Column(name = "ticket_type_id", nullable = false)
    UUID ticketTypeId;

    @Column(name = "ticket_type_name", nullable = false)
    String ticketTypeName;

    @Column(name = "seat_id", length = 64)
    String seatId;

    @Column(name = "section_id", length = 64)
    String sectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false, length = 32)
    SeatingMode seatingMode;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 2)
    BigDecimal faceValue;

    @Column(name = "asking_price", nullable = false, precision = 19, scale = 2)
    BigDecimal askingPrice;

    @Column(nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    ResaleListingStatus status;

    @Column(name = "reserved_until")
    Instant reservedUntil;

    @Version
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
