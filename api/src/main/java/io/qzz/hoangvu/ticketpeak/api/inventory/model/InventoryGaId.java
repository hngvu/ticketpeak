package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InventoryGaId implements Serializable {
    private UUID eventId;
    private String sectionId;
    private UUID offerId;
}
