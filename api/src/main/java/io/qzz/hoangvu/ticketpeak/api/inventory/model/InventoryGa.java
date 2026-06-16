package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@IdClass(InventoryGaId.class)
@Table(name = "inventory_ga")
public class InventoryGa {

    @Id
    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Id
    @Column(name = "section_id", nullable = false)
    String sectionId;

    @Id
    @Column(name = "offer_id", nullable = false)
    UUID offerId;

    // Capacity captured from section at ONSALE snapshot time.
    // Does not change after initialization.
    @Column(name = "total", nullable = false)
    int total;

    @Column(name = "available", nullable = false)
    int available;

    @Column(name = "held", nullable = false)
    int held;

    @Column(name = "sold", nullable = false)
    int sold;

    @Column(name = "killed", nullable = false)
    int killed;
}
