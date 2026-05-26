package io.qzz.hoangvu.ticketpeak.api.event.model;

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
@Table(name = "event")
public class Event {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "organization_id", nullable = false)
    UUID organizationId;

    @Column(name = "venue_id", nullable = false)
    UUID venueId;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "slug", unique = true, nullable = false)
    String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    EventStatus status;

    @Column(name = "start_at", nullable = false)
    Instant startAt;

    @Column(name = "end_at")
    Instant endAt;

    @Column(name = "timezone", nullable = false)
    String timezone;

    @Column(name = "sale_start_at")
    Instant saleStartAt;
    
    @Column(name = "sale_end_at")
    Instant saleEndAt;

    @Column(name = "restrict_single_seat", nullable = false)
    boolean restrictSingleSeat;

    @Column(name = "safe_tix_enabled", nullable = false)
    boolean safeTixEnabled;

    @Column(name = "transfer_enabled", nullable = false)
    boolean transferEnabled;

    @Column(name = "max_transfer_count", nullable = false)
    int maxTransferCount;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
