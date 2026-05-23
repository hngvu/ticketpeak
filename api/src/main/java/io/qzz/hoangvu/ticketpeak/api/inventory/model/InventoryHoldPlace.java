package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "inventory_hold_place")
public class InventoryHoldPlace {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "seat_id")
    String seatId;

    @Column(name = "area_id")
    String areaId;

    @Column(nullable = false)
    Integer quantity;

    @Column(name = "hold_token", nullable = false)
    String holdToken;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    Instant createdAt;
}
