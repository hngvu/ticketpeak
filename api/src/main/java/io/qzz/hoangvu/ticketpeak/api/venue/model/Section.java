package io.qzz.hoangvu.ticketpeak.api.venue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "section")
public class Section {

    @Id
    String id; // e.g., "0011 01", "1012 01"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    Manifest manifest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SectionType type;


    String color;

    String name; // display name, e.g. "Garden 1", "Orchestra Center"

    @Column(name = "level_id")
    String levelId;

    Integer capacity;



    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ui_data", columnDefinition = "jsonb")
    Map<String, Object> uiData;
}
