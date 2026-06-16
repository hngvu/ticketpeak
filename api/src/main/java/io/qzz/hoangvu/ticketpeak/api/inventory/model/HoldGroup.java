package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "hold_group")
public class HoldGroup {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "color")
    String color;

    @Column(name = "type")
    String type;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
