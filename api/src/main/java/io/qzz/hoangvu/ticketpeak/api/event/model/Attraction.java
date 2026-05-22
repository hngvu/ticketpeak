package io.qzz.hoangvu.ticketpeak.api.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
@Table(name = "attraction")
public class Attraction {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "slug", unique = true, nullable = false)
    String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    AttractionType type;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @org.springframework.data.annotation.CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @org.springframework.data.annotation.LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}

