# 010 Offer API - Ticket Types, Pricing, and Sale Windows

## Description
Implement the `offer` API layer on top of the event and venue domain.
This module should model the sellable ticket inventory for an event, inspired by Ticketmaster's offer and availability concepts.

From the Ticketmaster reference, the important concepts are:
- a stable internal record for the sellable ticket definition
- a public ticket type identifier
- offer name and description
- minimum and maximum purchasable quantity
- sellable quantities per transaction
- payment restrictions
- pricing data such as face value, charges, and currency
- reserved-seating mapping through section / price level relationships

The first version should cover:
- offer entity creation and persistence
- pricing and quantity rules
- sale window management
- event-scoped offer lookup
- support for general admission and reserved seating offers
- a clean migration that matches the entity model

## Acceptance Criteria
- [ ] The `offer` module has a matching Flyway migration and JPA entity set.
- [ ] Each offer belongs to exactly one event.
- [ ] Each offer has a stable internal primary key and a unique public ticket type identifier.
- [ ] Offer records store `name`, `description`, `currency`, `faceValue`, `restrictedPayment`, `eventTicketMinimum`, and `sellableQuantities`.
- [ ] Offer records support sale windows such as presale and general sale start/end timestamps.
- [ ] Offer records support a seating mode such as general admission or reserved seating.
- [ ] Reserved-seating offers can reference venue layout data such as `sectionId` and `priceLevelId`.
- [ ] Offer pricing can represent fee breakdowns in a way that matches Ticketmaster-style `charges`.
- [ ] Validations prevent negative prices, invalid quantities, and impossible sale windows.
- [ ] Offer slugs or public identifiers are unique where applicable.
- [ ] Public read APIs expose offer data through record DTOs wrapped in `ApiResponse<T>`.
- [ ] Integration tests cover entity persistence, uniqueness rules, pricing validation, sale window validation, and event scoping.

## Status
`done`

## Outcome
Implemented the offer domain with a new Flyway migration, JPA entity, repository, service, controller, DTOs, and integration coverage for persistence, uniqueness, validation, and event-scoped lookup. `./mvnw test` compiles successfully, but full Testcontainers execution could not complete in this environment because Docker is not available.
