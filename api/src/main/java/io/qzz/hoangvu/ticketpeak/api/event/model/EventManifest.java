package io.qzz.hoangvu.ticketpeak.api.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "event_manifest")
public class EventManifest {
    @Id
    @Column(name = "event_id")
    UUID eventId;

    @Column(name = "manifest_id", nullable = false)
    String manifestId;
}
