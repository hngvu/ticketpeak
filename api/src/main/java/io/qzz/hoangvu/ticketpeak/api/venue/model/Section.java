package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(SectionId.class)
@Table(name = "section")
public class Section {

    @Id
    String id; // e.g., "0011 01", "1012 01"

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    Manifest manifest;

    @Column(nullable = false)
    String description; // e.g., "Patio de Butacas", "Anfiteatro"

    String color;
}
