package io.qzz.hoangvu.ticketpeak.api.reservation.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Id;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reservation")
public class Reservation {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "account_id", nullable = false)
    UUID accountId;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    ReservationStatus status;

    @Column(nullable = false, length = 8)
    String currency;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<ReservationItem> items = new ArrayList<>();

    @Version
    @Column(nullable = false)
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
