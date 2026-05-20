package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "venue")
public class Venue {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    String address;

    @Column(nullable = false)
    String city;

    @Column(nullable = false)
    String country;

    @Column(precision = 9, scale = 6)
    BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    BigDecimal longitude;

    String phone;
    String email;
    String website;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "thumbnail_url")
    String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<String> images;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VenueStatus status;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;
}
