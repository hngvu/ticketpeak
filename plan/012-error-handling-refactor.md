# Plan: Error Handling Refactor — Per-Module Exception Factories

## Description

The current error handling approach throws `ApiException` with raw magic strings as error codes
scattered across every service class. This causes:

- Duplicate string literals across multiple modules (e.g. `"ORGANIZATION_NOT_FOUND"` appears in
  `OrganizationService`, `PermissionService`, and `OrganizationMemberService` independently).
- `"INVALID_STATE"` is reused for 7+ semantically distinct event state transition errors, making it
  impossible for clients to differentiate and render appropriate UI feedback.
- HTTP status codes are manually supplied at every throw site — prone to copy-paste errors.
- `GlobalExceptionHandler` does not handle `HttpMessageNotReadableException` (malformed JSON body),
  `DataIntegrityViolationException` (DB constraint violations), or unexpected runtime exceptions,
  causing unformatted 500 responses to leak to clients.

This refactor introduces a **per-module exception factory** pattern. Each module owns a single
`<Module>Exception` final class that encapsulates the HTTP status, error code string, and default
message for every error that module can produce. Services call static factory methods — no raw
strings, no HTTP status literals, no cross-module imports required.

`common/exception/` retains only `ApiException` as the shared base class (infrastructure, not
domain). No typed subclasses are added to `common/`.

No breaking changes to response shape or existing error codes visible to frontend clients.

## Pattern

```java
// event/exception/EventException.java
public final class EventException {
    private EventException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND,
                "EVENT_NOT_FOUND", "Event not found");
    }
    public static ApiException notPublishable() {
        return new ApiException(HttpStatus.BAD_REQUEST,
                "EVENT_NOT_PUBLISHABLE", "Only DRAFT events can be published");
    }
    public static ApiException cannotUpdateEnded() {
        return new ApiException(HttpStatus.BAD_REQUEST,
                "EVENT_CANNOT_UPDATE_ENDED", "Cannot update an ended or cancelled event");
    }
    // one method per distinct error ...
}

// EventService.java — call site
throw EventException.notFound();
throw EventException.notPublishable();
```

Each factory method is the single source of truth for that error's HTTP status, code string, and
message. Nothing leaks out of the module.

## Action Items

### 1. Create per-module exception factory classes

One `<Module>Exception` final class per module, located in `<module>/exception/`:

| File | Module | Factory methods |
|------|--------|----------------|
| `account/exception/AccountException.java` | account | `notFound()`, `emailAlreadyExists()` |
| `auth/exception/AuthException.java` | auth | `invalidCredentials()`, `invalidRefreshToken()`, `refreshTokenRevoked()`, `accountInactive()` |
| `event/exception/EventException.java` | event | `notFound()`, `notPublishable()`, `cannotUpdateEnded()`, `cannotChangeVenue()`, `cannotPostpone()`, `cannotCancel()`, `cannotReschedule()`, `cannotResume()`, `cannotStartSales()`, `invalidDates(String reason)`, `missingVenue()`, `noPublishedManifest()`, `venueNotFound()`, `attractionNotFound(UUID id)`, `classificationNotFound(UUID id)` |
| `venue/exception/VenueException.java` | venue | `notFound(UUID id)`, `manifestNotFound(String id)`, `manifestIdExists(String id)`, `invalidManifestStatus()`, `alreadyArchived()`, `gaAreaIdExists(String id)`, `rsAreaNotFound(String id)`, `rsAreaIdExists(String id)`, `seatRowNotFound(String id)`, `seatRowIdExists(String id)`, `seatRowNameDuplicate(String name)`, `seatIdExists(String id)`, `seatNameDuplicate(String name)` |
| `offer/exception/OfferException.java` | offer | `notFound()`, `ticketTypeAlreadyExists()`, `invalidEventState()`, `invalidCurrency()`, `invalidPrice()`, `invalidLimits()`, `invalidWindow(String reason)`, `invalidQuantity(String reason)`, `invalidSeating(String reason)`, `sectionNotFound(String id, String manifestId)`, `priceLevelNotFound(String id, String manifestId)`, `noPublishedManifest()` |
| `organization/exception/OrganizationException.java` | organization | `notFound()`, `invalidOwnerRole()`, `memberNotFound()`, `cannotRemoveOwner()`, `cannotLeaveAsOwner()`, `invalidStatusTransition(String reason)`, `notOrganizationMember()` |
| `iam/exception/IamException.java` | iam | `permissionNotFound()`, `permissionAlreadyExists()`, `permissionAlreadyGranted()`, `permissionGrantNotFound()`, `invalidPermissionScope()` |
| `inventory/exception/InventoryException.java` | inventory | (stub — add methods as inventory features are implemented) |

Factory methods that include runtime context (entity id, field value) accept it as a parameter and
interpolate into the message string:

```java
public static ApiException attractionNotFound(UUID id) {
    return new ApiException(HttpStatus.BAD_REQUEST,
            "EVENT_ATTRACTION_NOT_FOUND",
            "Attraction with id " + id + " does not exist");
}
```

### 2. Refactor all service throw sites

Replace every `throw new ApiException(HttpStatus.X, "RAW_STRING", "message")` with the module
factory call:

```java
// Before
throw new ApiException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Event not found");
throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Only DRAFT events can be published");

// After
throw EventException.notFound();
throw EventException.notPublishable();
```

Scope of changes: `AccountService`, `AuthService`, `EventService`, `VenueService`, `OfferService`,
`OrganizationService`, `OrganizationMemberService`, `OrganizationInvitationService`,
`PermissionService`, `InventoryService`.

Also remove the unused `import jakarta.persistence.EntityNotFoundException` from `AccountService`.

### 3. Expand `GlobalExceptionHandler`

Add handlers for exception types currently unhandled:

```java
// Malformed / unreadable JSON body
@ExceptionHandler(HttpMessageNotReadableException.class)
→ 400, error: "MALFORMED_REQUEST"

// DB unique constraint / FK violation leaking past service-layer checks
@ExceptionHandler(DataIntegrityViolationException.class)
→ 409, error: "DATA_INTEGRITY_VIOLATION"

// Catch-all — log and return structured 500 instead of Spring's default whitelist page
@ExceptionHandler(Exception.class)
→ 500, error: "INTERNAL_ERROR", logs full stack trace
```

Existing handlers (`ApiException`, `MethodArgumentNotValidException`, `BadCredentialsException`,
`AuthenticationException`, `AccessDeniedException`) remain unchanged.

## Acceptance Criteria

- No raw string error code literals remain in any `throw` statement inside service classes.
- No `HttpStatus` literals remain in any `throw` statement inside service classes.
- Each module's exception factory is the single source of truth for that module's error codes,
  HTTP statuses, and default messages — no duplication across modules.
- Event state transition errors use distinct, specific codes (e.g. `EVENT_NOT_PUBLISHABLE`) rather
  than the generic `INVALID_STATE`.
- `common/exception/` contains only `ApiException` — no typed subclasses added there.
- `GlobalExceptionHandler` handles `HttpMessageNotReadableException`, `DataIntegrityViolationException`,
  and a generic `Exception` fallback — all returning structured `ApiErrorResponse`.
- `ApiErrorResponse` shape is unchanged: `{ success, error, message, timestamp }`.
- All existing error code strings visible to the frontend remain identical to before the refactor
  (no renaming of codes already in use).
- `./mvnw compile -q` passes with zero warnings related to this change.
- `./mvnw verify` passes (all tests green).

## Status
`done`

## Outcome

All service throw sites now use per-module exception factory methods. No raw `new ApiException(...)` or `HttpStatus` literals remain in any service class. `GlobalExceptionHandler` expanded with handlers for `HttpMessageNotReadableException`, `DataIntegrityViolationException`, and generic `Exception`. All existing error codes preserved for frontend compatibility. `./mvnw compile -q` and `./mvnw verify` pass cleanly.

