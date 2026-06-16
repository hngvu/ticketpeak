package io.qzz.hoangvu.ticketpeak.api.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(HoldGroupSectionId.class)
@Table(name = "hold_group_section")
public class HoldGroupSection {

    @Id
    @Column(name = "hold_group_id", nullable = false)
    UUID holdGroupId;

    @Id
    @Column(name = "section_id", nullable = false)
    String sectionId;

    @Column(name = "quantity", nullable = false)
    int quantity;
}
