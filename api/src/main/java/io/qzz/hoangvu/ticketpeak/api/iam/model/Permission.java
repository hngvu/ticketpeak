package io.qzz.hoangvu.ticketpeak.api.iam.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {
    @Id
    @Column(nullable = false, unique = true)
    String code;
    @Column(nullable = false)
    String name;
    @Enumerated(EnumType.STRING)
    PermissionScope scope;
    String action;
    String resource;

}
