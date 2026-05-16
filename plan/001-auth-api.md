# 001 Auth API - JWT & Redis Whitelist

## Description
Implement the core Authentication module using JWT Access Tokens and Refresh Tokens. 
To enforce a "1 account = 1 active session" policy, we will use a Redis Whitelist approach.
When a user logs in, their Refresh Token is stored in Redis mapped to their Account ID. A new login will overwrite the old Refresh Token, effectively invalidating the previous session when it tries to refresh.

## JWT Payload Design
**Access Token:**
```json
{
  "sub": "uuid-of-account", // accountId
  "role": "BUYER|ORGANIZER|ADMIN",
  "iat": 1700000000,
  "exp": 1700000900 // 15 mins
}
```

**Refresh Token:**
```json
{
  "jti": "unique-uuid-for-this-token",
  "sub": "uuid-of-account",
  "type": "REFRESH",
  "iat": 1700000000,
  "exp": 1700604800 // 7 days
}
```

## Acceptance Criteria
- [x] Users can login using Email/Password and receive an Access Token (15m) and Refresh Token (7d).
- [x] Redis stores the whitelist as `auth:refresh:<accountId> -> <jti>`.
- [x] Logging in from a new device/browser overwrites the existing Redis key, logging out the previous device (once its short-lived Access Token expires).
- [x] Users can use their Refresh Token to get a new token pair via `/api/auth/refresh`.
- [x] `/api/auth/refresh` must validate the provided Refresh Token's `jti` against the one currently stored in Redis.
- [x] Users can logout via `/api/auth/logout`, which deletes their token from Redis.
- [x] Protected endpoints require a valid Access Token.

## Status
`done`

## Outcome
Implemented JWT login, refresh, logout, protected access, Redis refresh-token whitelisting, and a Flyway auth schema. Verified with `mvn test` and a dedicated auth integration suite.
