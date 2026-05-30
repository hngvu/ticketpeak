package io.qzz.hoangvu.ticketpeak.api.eventgroup.repository;

import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroupMember;
import io.qzz.hoangvu.ticketpeak.api.eventgroup.model.EventGroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventGroupMemberRepository extends JpaRepository<EventGroupMember, EventGroupMemberId> {
    List<EventGroupMember> findByEventGroupIdOrderByDisplayOrderAsc(UUID eventGroupId);
    List<EventGroupMember> findByEventId(UUID eventId);
    Optional<EventGroupMember> findByEventGroupIdAndEventId(UUID eventGroupId, UUID eventId);
    void deleteByEventGroupIdAndEventId(UUID eventGroupId, UUID eventId);
    void deleteByEventGroupId(UUID eventGroupId);
    void deleteByEventId(UUID eventId);
}
