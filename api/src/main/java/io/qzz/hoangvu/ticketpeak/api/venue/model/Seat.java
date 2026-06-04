package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "seat", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"row_id", "name"})
})
public class Seat {

    @Id
    String id; // e.g. custom string ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "row_id", nullable = false)
    SeatRow seatRow;

    @Column(nullable = false)
    String name; // e.g., "001", "5", "022"

    @Column(name = "position_x")
    Integer positionX;

    @Column(name = "position_y", nullable = false)
    Integer positionY;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SeatStatus status;

    Boolean accessibility;

    @Column(name = "obstructed_view")
    Boolean obstructedView;

    Boolean aisle;
}

