package io.qzz.hoangvu.ticketpeak.api.order.model;

import io.qzz.hoangvu.ticketpeak.api.offer.model.OfferCharge;
import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seating_mode", nullable = false, length = 32)
    SeatingMode seatingMode;

    @Column(name = "area_id", length = 64)
    String sectionId;

    @Column(name = "seat_id", length = 64)
    String seatId;

    @Column(nullable = false)
    int qty;

    @Column(name = "unit_face_value", nullable = false, precision = 19, scale = 2)
    BigDecimal unitFaceValue;

    @Column(name = "unit_total_price", nullable = false, precision = 19, scale = 2)
    BigDecimal unitTotalPrice;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    BigDecimal lineTotal;

    @Column(nullable = false, length = 8)
    String currency;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    List<OfferCharge> charges = List.of();
}
