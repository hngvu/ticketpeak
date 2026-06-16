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
public class HoldGroupSectionId implements Serializable {
    private UUID holdGroupId;
    private String sectionId;
}
