package io.qzz.hoangvu.ticketpeak.api.event.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAttractionId implements Serializable {
    private UUID eventId;
    private UUID attractionId;
}
