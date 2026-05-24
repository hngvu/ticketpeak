package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(InventorySeatId.class)
@Table(name = "inventory_seat")
public class InventorySeat {

    @Id
    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Id
    @Column(name = "seat_id", nullable = false)
    String seatId;

    @Column(name = "offer_id")
    UUID offerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SeatInventoryStatus status; // AVAILABLE, HELD, SOLD
}
