package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "seat_row")
public class SeatRow {

    @Id
    String id; // e.g. custom string ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rs_area_id", nullable = false)
    RSArea rsArea;

    @Column(nullable = false)
    String name; // e.g., "01", "A", "BB"

    @Column(name = "position_y")
    Integer positionY;
}
