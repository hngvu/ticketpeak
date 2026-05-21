package io.qzz.hoangvu.ticketpeak.api.event.repository;

import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventSpecification {

    public static Specification<Event> filterEvents(
            String query,
            List<EventStatus> statuses,
            List<EventStatus> excludeStatuses,
            UUID venueId,
            UUID organizationId,
            Instant startAfter,
            Instant startBefore,
            Instant endAfter,
            Instant endBefore,
            List<UUID> venueIdsByQuery,
            List<UUID> eventIdsForClassificationsByQuery,
            List<UUID> eventIdsForAttractionsByQuery,
            List<UUID> explicitEventIdsForClassifications,
            List<UUID> venueIdsByLocation,
            List<UUID> explicitEventIdsForAttractions
    ) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase() + "%";
                List<Predicate> queryPredicates = new ArrayList<>();

                queryPredicates.add(cb.like(cb.lower(root.get("title")), pattern));
                queryPredicates.add(cb.like(cb.lower(root.get("description")), pattern));

                if (venueIdsByQuery != null && !venueIdsByQuery.isEmpty()) {
                    queryPredicates.add(root.get("venueId").in(venueIdsByQuery));
                }

                if (eventIdsForClassificationsByQuery != null && !eventIdsForClassificationsByQuery.isEmpty()) {
                    queryPredicates.add(root.get("id").in(eventIdsForClassificationsByQuery));
                }

                if (eventIdsForAttractionsByQuery != null && !eventIdsForAttractionsByQuery.isEmpty()) {
                    queryPredicates.add(root.get("id").in(eventIdsForAttractionsByQuery));
                }

                predicates.add(cb.or(queryPredicates.toArray(new Predicate[0])));
            }

            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            if (excludeStatuses != null && !excludeStatuses.isEmpty()) {
                predicates.add(cb.not(root.get("status").in(excludeStatuses)));
            }

            if (venueId != null) {
                predicates.add(cb.equal(root.get("venueId"), venueId));
            }

            if (organizationId != null) {
                predicates.add(cb.equal(root.get("organizationId"), organizationId));
            }

            if (startAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), startAfter));
            }

            if (startBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), startBefore));
            }

            if (endAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endAt"), endAfter));
            }

            if (endBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endAt"), endBefore));
            }

            if (explicitEventIdsForClassifications != null) {
                if (explicitEventIdsForClassifications.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("id").in(explicitEventIdsForClassifications));
                }
            }

            if (venueIdsByLocation != null) {
                if (venueIdsByLocation.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("venueId").in(venueIdsByLocation));
                }
            }

            if (explicitEventIdsForAttractions != null) {
                if (explicitEventIdsForAttractions.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("id").in(explicitEventIdsForAttractions));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
