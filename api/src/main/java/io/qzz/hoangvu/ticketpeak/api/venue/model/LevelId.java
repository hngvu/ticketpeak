package io.qzz.hoangvu.ticketpeak.api.venue.model;

import lombok.*;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class LevelId implements Serializable {
    private String id;
    private String manifest; // Must match the field name of Manifest in Level and its PK type (String)
}
