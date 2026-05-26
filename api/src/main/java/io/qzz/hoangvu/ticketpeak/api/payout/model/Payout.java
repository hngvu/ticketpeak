package io.qzz.hoangvu.ticketpeak.api.payout.model;

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
import java.util.UUID;

@Entity
@Table(name = "payout")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Payout {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "resale_listing_id")
    UUID resaleListingId;

    @Column(name = "seller_id", nullable = false)
    UUID sellerId;

    @Column(name = "payout_method_id", nullable = false)
    UUID payoutMethodId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payout_method_snapshot", columnDefinition = "jsonb", nullable = false)
    PayoutMethodSnapshot payoutMethodSnapshot;

    @Column(name = "gross_amount", nullable = false)
    BigDecimal grossAmount;

    @Column(name = "platform_fee_percent", nullable = false)
    BigDecimal platformFeePercent;

    @Column(name = "platform_fee_amount", nullable = false)
    BigDecimal platformFeeAmount;

    @Column(name = "net_amount", nullable = false)
    BigDecimal netAmount;

    @Column(nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    PayoutStatus status;

    @Column(name = "scheduled_after", nullable = false)
    Instant scheduledAfter;

    @Column(name = "processed_at")
    Instant processedAt;

    @Column(name = "external_ref")
    String externalRef;

    @Column(columnDefinition = "TEXT")
    String note;

    @Version
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
