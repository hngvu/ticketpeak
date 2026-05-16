# 002 Account API - Profile Management

## Description
Implement the `account` API layer on top of the existing account persistence model.
The goal is to support the core account lifecycle that belongs to this module: account creation, reading the current user's profile, and updating allowed profile fields without exposing sensitive data or mixing profile concerns into authentication endpoints.
This work should cover the core account flow for the current `Account` entity:
account creation plus self-service profile fields such as `firstName`, `lastName`, `avatarUrl`, `birthDate`, `gender`, `cityId`, and `countryCode`.
Server-managed or sensitive data such as `status`, `role`, `email`, `password`, and `metadata` must remain protected.

## Acceptance Criteria
- [ ] Unauthenticated users can create an account through a dedicated registration endpoint.
- [ ] Account creation validates required fields, rejects duplicate emails, hashes the password, and sets safe default `role` and `status` values.
- [ ] Authenticated users can retrieve their own account profile through a dedicated `/api/accounts/me` endpoint.
- [ ] Authenticated users can update only the allowed profile fields for their own account.
- [ ] Sensitive or server-managed fields such as `email`, `password`, `role`, `status`, `metadata`, `createdAt`, and `updatedAt` cannot be modified through the public account API.
- [ ] All request and response payloads use record DTOs and `ApiResponse<T>` envelopes.
- [ ] Request validation rejects invalid profile data with the standard `VALIDATION_FAILED` response shape.
- [ ] Unauthorized requests are rejected, and authenticated users cannot access or modify another account’s profile.
- [ ] Integration tests cover successful account creation, profile read/update, validation failures, unauthorized access, duplicate-email rejection, and persistence of allowed field changes.

## Status
`done`

## Outcome
Implemented account registration, self-profile read/update, field-level validation, and access control on top of the JWT auth flow. Verified with `mvn test` and integration coverage for registration, duplicate email rejection, profile read/update, validation failures, and unauthorized access.
