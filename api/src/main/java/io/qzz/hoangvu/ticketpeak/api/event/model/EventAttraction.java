package io.qzz.hoangvu.ticketpeak.api.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "event_attraction")
@IdClass(EventAttractionId.class)
public class EventAttraction {
    @Id
    @Column(name = "event_id")
    UUID eventId;

    @Id
    @Column(name = "attraction_id")
    UUID attractionId;
}
