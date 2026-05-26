package io.qzz.hoangvu.ticketpeak.api.ticket.model;

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
@Table(name = "ticket_transfer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class TicketTransfer {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "ticket_id", nullable = false)
    UUID ticketId;

    @Column(name = "sender_id", nullable = false)
    UUID senderId;

    @Column(name = "recipient_id", nullable = false)
    UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    TicketTransferStatus status;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
