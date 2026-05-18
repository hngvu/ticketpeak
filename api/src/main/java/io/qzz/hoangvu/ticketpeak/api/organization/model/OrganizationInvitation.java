package io.qzz.hoangvu.ticketpeak.api.organization.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
@Table(name = "organization_invitation")
public class OrganizationInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    // Who is being invited (soft reference to Account)
    @Column(name = "invitee_account_id", nullable = false)
    UUID inviteeAccountId;

    // Who sent the invitation (soft reference to Account)
    @Column(name = "invited_by", nullable = false)
    UUID invitedBy;

    // Unique token embedded in the invitation link sent via email
    @Column(nullable = false, unique = true, updatable = false)
    String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OrganizationInvitationStatus status;

    // Invitation link expires after this time
    @Column(nullable = false)
    Instant expiresAt;

    // Timestamp when invitee responded (accepted or rejected)
    Instant respondedAt;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;
}
