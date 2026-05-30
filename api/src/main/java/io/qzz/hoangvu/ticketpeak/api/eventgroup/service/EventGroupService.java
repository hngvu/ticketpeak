package io.qzz.hoangvu.ticketpeak.api.eventgroup.service;

import io.qzz.hoangvu.ticketpeak.api.event.dto.EventResponse;
import io.qzz.hoangvu.ticketpeak.api.event.model.Event;
import io.qzz.hoangvu.ticketpeak.api.event.model.EventStatus;
import io.qzz.hoangvu.ticketpeak.api.event.repository.EventRepository;
import io.qzz.hoangvu.ticketpeak.api.event.service.EventService;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.dto.*;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.exception.EventGroupException;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroup;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroupMember;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.repository.EventGroupMemberRepository;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.repository.EventGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EventGroupService {

    private final EventGroupRepository eventGroupRepository;
    private final EventGroupMemberRepository eventGroupMemberRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    public EventGroupService(
            EventGroupRepository eventGroupRepository,
            EventGroupMemberRepository eventGroupMemberRepository,
            EventRepository eventRepository,
            EventService eventService
    ) {
        this.eventGroupRepository = eventGroupRepository;
        this.eventGroupMemberRepository = eventGroupMemberRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    @Transactional
    public EventGroupResponse createGroup(CreateEventGroupRequest req) {
        String slug = req.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugify(req.name());
        }
        slug = slug.toLowerCase();

        if (eventGroupRepository.existsBySlug(slug)) {
            throw EventGroupException.slugAlreadyExists();
        }

        UUID groupId = UUID.randomUUID();

        EventGroup eventGroup = EventGroup.builder()
                .id(groupId)
                .organizationId(req.organizationId())
                .name(req.name())
                .slug(slug)
                .description(req.description())
                .imageUrl(req.imageUrl())
                .isActive(true)
                .build();

        EventGroup savedGroup = eventGroupRepository.save(eventGroup);

        List<EventResponse> eventResponses = new ArrayList<>();
        if (req.eventIds() != null && !req.eventIds().isEmpty()) {
            List<UUID> uniqueEventIds = req.eventIds().stream().distinct().toList();
            List<Event> events = eventRepository.findAllById(uniqueEventIds);

            if (events.size() != uniqueEventIds.size()) {
                Set<UUID> foundIds = events.stream().map(Event::getId).collect(Collectors.toSet());
                UUID missingId = uniqueEventIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .findFirst()
                        .orElse(null);
                throw EventGroupException.eventNotFound(missingId);
            }

            // Create members in order
            List<EventGroupMember> members = new ArrayList<>();
            for (int i = 0; i < uniqueEventIds.size(); i++) {
                members.add(EventGroupMember.builder()
                        .eventGroupId(groupId)
                        .eventId(uniqueEventIds.get(i))
                        .displayOrder(i)
                        .build());
            }
            eventGroupMemberRepository.saveAll(members);

            // Re-order events based on requested list
            Map<UUID, Event> eventMap = events.stream()
                    .collect(Collectors.toMap(Event::getId, Function.identity()));
            List<Event> orderedEvents = uniqueEventIds.stream()
                    .map(eventMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            eventResponses = eventService.convertToResponses(orderedEvents);
        }

        return EventGroupResponse.from(savedGroup, eventResponses);
    }

    @Transactional
    public EventGroupResponse updateGroup(UUID groupId, UpdateEventGroupRequest req) {
        EventGroup eventGroup = eventGroupRepository.findById(groupId)
                .orElseThrow(EventGroupException::notFound);

        String slug = req.slug();
        if (slug != null && !slug.isBlank()) {
            slug = slug.toLowerCase();
            if (!slug.equals(eventGroup.getSlug()) && eventGroupRepository.existsBySlug(slug)) {
                throw EventGroupException.slugAlreadyExists();
            }
            eventGroup.setSlug(slug);
        }

        eventGroup.setName(req.name());
        eventGroup.setDescription(req.description());
        eventGroup.setImageUrl(req.imageUrl());
        if (req.isActive() != null) {
            eventGroup.setIsActive(req.isActive());
        }

        EventGroup savedGroup = eventGroupRepository.save(eventGroup);

        if (req.eventIds() != null) {
            eventGroupMemberRepository.deleteByEventGroupId(groupId);

            List<UUID> uniqueEventIds = req.eventIds().stream().distinct().toList();
            if (!uniqueEventIds.isEmpty()) {
                List<Event> events = eventRepository.findAllById(uniqueEventIds);

                if (events.size() != uniqueEventIds.size()) {
                    Set<UUID> foundIds = events.stream().map(Event::getId).collect(Collectors.toSet());
                    UUID missingId = uniqueEventIds.stream()
                            .filter(id -> !foundIds.contains(id))
                            .findFirst()
                            .orElse(null);
                    throw EventGroupException.eventNotFound(missingId);
                }

                List<EventGroupMember> members = new ArrayList<>();
                for (int i = 0; i < uniqueEventIds.size(); i++) {
                    members.add(EventGroupMember.builder()
                            .eventGroupId(groupId)
                            .eventId(uniqueEventIds.get(i))
                            .displayOrder(i)
                            .build());
                }
                eventGroupMemberRepository.saveAll(members);
            }
        }

        return getGroupResponse(savedGroup, true);
    }

    @Transactional
    public void addEventToGroup(UUID groupId, UUID eventId, Integer displayOrder) {
        EventGroup eventGroup = eventGroupRepository.findById(groupId)
                .orElseThrow(EventGroupException::notFound);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> EventGroupException.eventNotFound(eventId));

        Optional<EventGroupMember> existing = eventGroupMemberRepository.findByEventGroupIdAndEventId(groupId, eventId);
        if (existing.isPresent()) {
            throw EventGroupException.eventAlreadyInGroup();
        }

        int finalOrder = 0;
        if (displayOrder != null) {
            finalOrder = displayOrder;
        } else {
            List<EventGroupMember> currentMembers = eventGroupMemberRepository.findByEventGroupIdOrderByDisplayOrderAsc(groupId);
            if (!currentMembers.isEmpty()) {
                finalOrder = currentMembers.get(currentMembers.size() - 1).getDisplayOrder() + 1;
            }
        }

        EventGroupMember member = EventGroupMember.builder()
                .eventGroupId(groupId)
                .eventId(eventId)
                .displayOrder(finalOrder)
                .build();

        eventGroupMemberRepository.save(member);
    }

    @Transactional
    public void removeEventFromGroup(UUID groupId, UUID eventId) {
        if (!eventGroupRepository.existsById(groupId)) {
            throw EventGroupException.notFound();
        }

        Optional<EventGroupMember> existing = eventGroupMemberRepository.findByEventGroupIdAndEventId(groupId, eventId);
        if (existing.isEmpty()) {
            throw EventGroupException.eventNotInGroup();
        }

        eventGroupMemberRepository.deleteByEventGroupIdAndEventId(groupId, eventId);
    }

    @Transactional(readOnly = true)
    public EventGroupResponse getGroup(UUID groupId, boolean isPartner) {
        EventGroup eventGroup = eventGroupRepository.findById(groupId)
                .orElseThrow(EventGroupException::notFound);
        return getGroupResponse(eventGroup, isPartner);
    }

    @Transactional(readOnly = true)
    public EventGroupResponse getGroupBySlug(String slug, boolean isPartner) {
        EventGroup eventGroup = eventGroupRepository.findBySlug(slug)
                .orElseThrow(EventGroupException::notFound);
        return getGroupResponse(eventGroup, isPartner);
    }

    @Transactional(readOnly = true)
    public Page<EventGroupResponse> searchGroups(String query, UUID organizationId, Pageable pageable) {
        Page<EventGroup> groupsPage = eventGroupRepository.searchGroups(query, organizationId, pageable);
        return groupsPage.map(eg -> getGroupResponse(eg, true));
    }

    @Transactional
    public void deleteGroup(UUID groupId) {
        if (!eventGroupRepository.existsById(groupId)) {
            throw EventGroupException.notFound();
        }
        eventGroupMemberRepository.deleteByEventGroupId(groupId);
        eventGroupRepository.deleteById(groupId);
    }

    private EventGroupResponse getGroupResponse(EventGroup eg, boolean isPartner) {
        List<EventGroupMember> members = eventGroupMemberRepository.findByEventGroupIdOrderByDisplayOrderAsc(eg.getId());
        if (members.isEmpty()) {
            return EventGroupResponse.from(eg, List.of());
        }

        List<UUID> eventIds = members.stream().map(EventGroupMember::getEventId).toList();
        List<Event> events = eventRepository.findAllById(eventIds);

        Map<UUID, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<Event> orderedEvents = new ArrayList<>();
        for (EventGroupMember m : members) {
            Event e = eventMap.get(m.getEventId());
            if (e != null) {
                // If not partner (public view), skip draft events
                if (!isPartner && e.getStatus() == EventStatus.DRAFT) {
                    continue;
                }
                orderedEvents.add(e);
            }
        }

        List<EventResponse> eventResponses = eventService.convertToResponses(orderedEvents);
        return EventGroupResponse.from(eg, eventResponses);
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
