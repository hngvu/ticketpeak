package io.qzz.hoangvu.ticketpeak.api.eventgroup.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventGroupMemberId implements Serializable {
    private UUID eventGroupId;
    private UUID eventId;
}
