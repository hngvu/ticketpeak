package io.qzz.hoangvu.ticketpeak.api.organization.model;

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
@Table(name = "organization")
public class Organization {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(unique = true, nullable = false)
    String slug; // URL-friendly identifier, e.g. "sun-world-entertainment"

    String bio;
    String logoUrl;
    String websiteUrl;

    // Contact
    String email;
    String phone;

    // Location (soft reference to common module)
    Integer cityId;
    String countryCode;

    // Ownership - soft reference to Account module
    @Column(nullable = false)
    UUID ownerAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OrganizationStatus status;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;
}
