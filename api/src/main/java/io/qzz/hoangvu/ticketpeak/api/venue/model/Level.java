package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(LevelId.class)
@Table(name = "venue_level") // "level" is not technically reserved, but "venue_level" is extremely clean and safe
public class Level {

    @Id
    String id; // e.g., "1", "2"

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    Manifest manifest;

    @Column(nullable = false)
    String description; // e.g., "Planta 1", "Orchestra"
}
