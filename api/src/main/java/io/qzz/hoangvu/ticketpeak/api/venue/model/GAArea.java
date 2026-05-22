package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "ga_area")
public class GAArea {

    @Id
    String id; // e.g. custom string ID

    @Column(name = "manifest_id", nullable = false)
    String manifestId;

    @Column(name = "level_id", nullable = false)
    String levelId;

    @Column(name = "section_id", nullable = false)
    String sectionId;

    @Column(name = "price_level_id", nullable = false)
    String priceLevelId;

    @Column(nullable = false)
    Integer capacity;

}
