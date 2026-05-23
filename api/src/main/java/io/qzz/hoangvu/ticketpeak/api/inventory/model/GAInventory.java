package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "ga_inventory")
public class GAInventory {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "area_id", nullable = false)
    String areaId;

    @Column(nullable = false)
    Integer capacity;

    @Column(nullable = false)
    Integer held;

    @Column(nullable = false)
    Integer purchased;

    @Version
    Long version;
}
