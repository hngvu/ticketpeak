package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(PriceLevelId.class)
@Table(name = "price_level")
public class PriceLevel {

    @Id
    String id; // e.g., "011 01", "VIP"

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    Manifest manifest;

    String description; // e.g., "VIP", "Category A"

    String color;
}
