package io.qzz.hoangvu.ticketpeak.api.common.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false)
    String name;
    String slug;
    String latitude;
    String longitude;
    String timezone;
    boolean isActive;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code")
    Country country;
}

