package io.qzz.hoangvu.ticketpeak.api.venue.model;

import lombok.*;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class PriceLevelId implements Serializable {
    private String id;
    private String manifest;
}
