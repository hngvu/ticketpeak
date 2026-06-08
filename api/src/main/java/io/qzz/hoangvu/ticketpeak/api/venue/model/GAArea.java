package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

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

    @Column(name = "price_level_id")
    String priceLevelId;

    @Column(nullable = false)
    Integer capacity;

    Integer x;

    Integer y;

    Integer width;

    Integer height;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<List<Double>> polygon; // e.g., [[x1,y1],[x2,y2],...]

}
