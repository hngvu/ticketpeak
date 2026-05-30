package io.qzz.hoangvu.ticketpeak.api.eventgroup.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "event_group_member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EventGroupMemberId.class)
public class EventGroupMember {

    @Id
    @Column(name = "event_group_id", nullable = false)
    private UUID eventGroupId;

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
