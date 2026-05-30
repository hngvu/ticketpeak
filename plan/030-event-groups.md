# 030 — Event Group (Series / Tour) Feature Module — API

## Description

Implement the **Event Group** (Series / Tour) feature module in the Spring Boot backend. This module enables event organizers (partners) to group multiple distinct events together under a single logical container (e.g. a multi-city concert tour, a multi-day festival, or a recurring show series).

The implementation includes:
- A new database migration `V39__create_event_group_schema.sql` creating the `event_group` and `event_group_member` tables.
- JPA entities `EventGroup.java` and `EventGroupMember.java`.
- Repositories `EventGroupRepository.java` and `EventGroupMemberRepository.java`.
- Exception class `EventGroupException.java` adhering to the Exception Factory Pattern.
- Request/Response records (DTOs) for creating, updating, and returning group details.
- Service layer `EventGroupService.java` integrating with `EventService` to reuse high-fidelity event response mapping.
- Three REST controllers: `EventGroupController` (Public), `PartnerEventGroupController` (Organizers), and `OpsEventGroupController` (Platform Admins).

---

## Files Liên Quan

| File | Thay đổi |
|---|---|
| [NEW] `api/src/main/resources/db/migration/V39__create_event_group_schema.sql` | Migration SQL tạo schema bảng nhóm sự kiện và bảng liên kết thành viên |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/model/EventGroup.java` | JPA Entity đại diện cho nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/model/EventGroupMember.java` | JPA Entity bảng liên kết thành viên |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/model/EventGroupMemberId.java` | Khóa chính phức hợp cho bảng liên kết thành viên |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/repository/EventGroupRepository.java` | Repository quản lý nhóm sự kiện, hỗ trợ search/phân trang |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/repository/EventGroupMemberRepository.java` | Repository quản lý liên kết thành viên nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/exception/EventGroupException.java` | Storage factory exceptions cho module nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/dto/CreateEventGroupRequest.java` | DTO yêu cầu tạo nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/dto/UpdateEventGroupRequest.java` | DTO yêu cầu cập nhật nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/dto/AddEventToGroupRequest.java` | DTO yêu cầu thêm sự kiện vào nhóm |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/dto/EventGroupResponse.java` | DTO phản hồi chi tiết nhóm sự kiện kèm danh sách sự kiện thành viên |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/service/EventGroupService.java` | Service chứa logic nghiệp vụ chính của module nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/controller/EventGroupController.java` | REST Controller công khai tìm kiếm / lấy chi tiết nhóm sự kiện |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/controller/PartnerEventGroupController.java` | REST Controller cho Organizer quản trị nhóm sự kiện thuộc sở hữu |
| [NEW] `io/qzz/hoangvu/ticketpeak/api/eventgroup/controller/OpsEventGroupController.java` | REST Controller cho Platform Admin toàn quyền quản trị |

---

## Technical Specifications

### DB Migration (`V39__create_event_group_schema.sql`)
```sql
CREATE TABLE event_group (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    image_url       VARCHAR(255),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_group_member (
    event_group_id UUID NOT NULL REFERENCES event_group(id) ON DELETE CASCADE,
    event_id       UUID NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    display_order  INT NOT NULL DEFAULT 0,
    PRIMARY KEY (event_group_id, event_id)
);
```

### EventGroupException.java (Exception Factory Pattern)
```java
package io.qzz.hoangvu.ticketpeak.api.eventgroup.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class EventGroupException {
    private EventGroupException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "EVENT_GROUP_NOT_FOUND", "Event Group not found");
    }

    public static ApiException slugAlreadyExists() {
        return new ApiException(HttpStatus.CONFLICT, "EVENT_GROUP_SLUG_ALREADY_EXISTS", "Event Group slug already exists");
    }

    public static ApiException eventNotFound(java.util.UUID id) {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_NOT_FOUND", "Event with id " + id + " does not exist");
    }

    public static ApiException eventAlreadyInGroup() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_ALREADY_IN_GROUP", "Event is already in this group");
    }

    public static ApiException eventNotInGroup() {
        return new ApiException(HttpStatus.BAD_REQUEST, "EVENT_NOT_IN_GROUP", "Event is not a member of this group");
    }
}
```

---

## Acceptance Criteria

- [ ] Spring Boot backend compiles successfully with `BUILD SUCCESS` via `./mvnw compile -q`.
- [ ] Flyway database migrations execute flawlessly on startup, creating the schema tables cleanly.
- [ ] Programmatic validation checks unique constraint conflicts (existsBySlug) in the service layer, throwing `EventGroupException.slugAlreadyExists()` instead of unhandled 500 errors.
- [ ] `PartnerEventGroupController` enforces strict organization membership access, ensuring organizers can only manage groups belonging to organizations they actively own or represent.
- [ ] `OpsEventGroupController` restricts all endpoint actions exclusively to accounts possessing `ROLE_ADMIN` access.
- [ ] End-to-end integration tests or direct REST validations prove the flow: Create Group -> Add Events -> Retrieve Group by ID -> Remove Event -> Delete Group.

---

## Status

`done`

## Outcome

All components of the Event Group feature backend module have been fully implemented:
1. Migration schema `V40__create_event_group_schema.sql` was integrated.
2. JPA models (`EventGroup`, `EventGroupMember` with composite keys) and Repositories are in place.
3. Transactional business logic resides in `EventGroupService`.
4. Exposed endpoints via Public, Partner, and Admin controllers, strictly adhering to role/org permissions.
5. High-fidelity integration test suite `EventGroupControllerIT` compiles, runs, and passes successfully.
