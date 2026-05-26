package io.qzz.hoangvu.ticketpeak.api.payout.model;

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
@Table(name = "payout_method")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class PayoutMethod {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "account_id", nullable = false)
    UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    PayoutMethodType type;

    @Column(name = "is_primary", nullable = false)
    boolean isPrimary;

    @Column(name = "holder_name", nullable = false)
    String holderName;

    @Column(name = "bank_code", length = 16)
    String bankCode;

    @Column(name = "account_number_enc", nullable = false, columnDefinition = "TEXT")
    String accountNumberEnc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    PayoutMethodStatus status;

    @Column(name = "verified_at")
    Instant verifiedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    @Column(nullable = false)
    Long version;
}
