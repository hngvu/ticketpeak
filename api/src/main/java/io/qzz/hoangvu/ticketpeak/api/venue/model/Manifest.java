package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import java.util.Map;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "manifest")
public class Manifest {

    @Id
    @Column(nullable = false, unique = true)
    String id; // Custom String ID (e.g., "000001002")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", referencedColumnName = "id", nullable = false)
    Venue venue;

    @Column(nullable = false)
    String description; // e.g. "Sala Mixta", "Concert Layout"

    @Column(name = "total_capacity", nullable = false)
    Integer totalCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ManifestStatus status;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<Map<String, Object>> objects;
}
