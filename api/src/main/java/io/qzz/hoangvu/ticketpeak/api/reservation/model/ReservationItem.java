package io.qzz.hoangvu.ticketpeak.api.reservation.model;

import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "reservation_item")
public class ReservationItem {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    Reservation reservation;

    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false, length = 32)
    SeatingMode seatingMode;

    @Column(name = "section_id", length = 64)
    String sectionId;

    @Column(name = "seat_id", length = 64)
    String seatId;

    @Column(nullable = false)
    int qty;

    @Column(name = "unit_face_value", nullable = false, precision = 19, scale = 2)
    BigDecimal unitFaceValue;

    @Column(nullable = false, length = 8)
    String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    List<OfferCharge> charges;
}
