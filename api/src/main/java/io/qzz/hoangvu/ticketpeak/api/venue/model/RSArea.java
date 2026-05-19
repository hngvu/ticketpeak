package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "rs_area")
public class RSArea {

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

    // --- Relationships (Read-Only to avoid duplicate column mappings) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", insertable = false, updatable = false)
    Manifest manifest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "manifest_id", referencedColumnName = "manifest_id", insertable = false, updatable = false),
        @JoinColumn(name = "level_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "manifest_id", referencedColumnName = "manifest_id", insertable = false, updatable = false),
        @JoinColumn(name = "section_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "manifest_id", referencedColumnName = "manifest_id", insertable = false, updatable = false),
        @JoinColumn(name = "price_level_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    PriceLevel priceLevel;
}
