package io.qzz.hoangvu.ticketpeak.api.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payment")
public class Payment {

    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false, updatable = false, nullable = false)
    UUID id;

    @Column(name = "reservation_id", nullable = false)
    UUID reservationId;

    @Column(name = "account_id", nullable = false)
    UUID accountId;

    @Column(name = "event_id", nullable = false)
    UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal amount;

    @Column(nullable = false, length = 8)
    String currency;

    @Column(name = "gateway_transaction_id", length = 256)
    String gatewayTransactionId;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_payload", columnDefinition = "jsonb", nullable = false)
    Map<String, Object> gatewayPayload = Map.of();

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb", nullable = false)
    Map<String, Object> gatewayResponse = Map.of();

    @Column(name = "refunded_at")
    Instant refundedAt;

    @Column(name = "refunded_amount", precision = 19, scale = 2)
    BigDecimal refundedAmount;

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
