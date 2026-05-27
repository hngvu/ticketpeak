package io.qzz.hoangvu.ticketpeak.api.resale.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resale_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ResaleOrder {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false)
    UUID id;

    @Column(name = "resale_listing_id", nullable = false)
    UUID resaleListingId;

    @Column(name = "buyer_id", nullable = false)
    UUID buyerId;

    @Column(name = "payment_id")
    UUID paymentId;

    @Column(name = "asking_price", nullable = false, precision = 19, scale = 2)
    BigDecimal askingPrice;

    @Column(name = "platform_fee_percent", nullable = false, precision = 5, scale = 2)
    BigDecimal platformFeePercent;

    @Column(name = "platform_fee_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal platformFeeAmount;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal netAmount;

    @Column(nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    ResaleOrderStatus status;

    @Version
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
