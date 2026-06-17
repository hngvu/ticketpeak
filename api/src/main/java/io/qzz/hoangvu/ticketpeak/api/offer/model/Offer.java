package io.qzz.hoangvu.ticketpeak.api.offer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "offer")
public class Offer {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "ticket_type_id", nullable = false)
    UUID ticketTypeId;

    @Column(nullable = false, length = 64)
    String code;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false, length = 8)
    String currency;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 2)
    BigDecimal faceValue;

    @Column(name = "restricted_payment", nullable = false)
    boolean restrictedPayment;

    @Column(name = "event_ticket_minimum", nullable = false)
    Integer eventTicketMinimum;

    @Column(name = "capacity_cap", nullable = false)
    Integer capacityCap;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sellable_quantities", columnDefinition = "jsonb", nullable = false)
    List<Integer> sellableQuantities;

    @Builder.Default
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<OfferSaleWindow> saleWindows = new java.util.ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false)
    SeatingMode seatingMode;

    @Column(name = "section_id")
    String sectionId;

    @Column(name = "price_level_id")
    String priceLevelId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    List<OfferCharge> charges;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;
}
