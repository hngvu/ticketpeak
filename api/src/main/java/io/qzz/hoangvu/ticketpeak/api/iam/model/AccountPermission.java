package io.qzz.hoangvu.ticketpeak.api.iam.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "account_permission", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id", "permission_code"})
})
public class AccountPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "account_id", nullable = false)
    UUID accountId; // Soft reference to Account module

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_code", nullable = false)
    Permission permission;

    @Builder.Default
    @Column(nullable = false)
    Boolean active = true;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant grantedAt;

    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;

    @Column(name = "granted_by")
    UUID grantedBy; // UUID of the admin who granted this permission
}
