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
@Table(name = "organization_member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "account_id"})
})
public class OrganizationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    // Soft reference to Account module
    @Column(name = "account_id")
    UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OrganizationMemberStatus status;

    // Who invited this member (soft reference to Account)
    @Column(name = "invited_by")
    UUID invitedBy;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant joinedAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;
}
