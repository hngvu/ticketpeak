package io.qzz.hoangvu.ticketpeak.api.account.model;

import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID DEFAULT uuidv7()", insertable = false,updatable = false, nullable = false)
    UUID id;


    @Column(unique = true, nullable = false)
    String email;
    @Column(nullable = false)
    String password;

    // profile
    String firstName;
    String lastName;
    String avatarUrl;
    LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    Gender gender;
    Integer cityId;
    String countryCode;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_role", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    Set<Role> roles = new HashSet<>();
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, Object> metadata;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    Instant createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    Instant updatedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AccountStatus status;
}
