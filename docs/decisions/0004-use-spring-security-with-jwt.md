# 4. Use Spring Security with JWT for Authentication

**Date:** 2026-07-03

## Status

Accepted

## Context

The application requires stateless authentication for REST APIs. Sessions are not suitable due to the distributed nature and the need to support mobile/SPA clients.

## Decision

- Use **Spring Security** for authentication and authorization.
- Use **JWT** (JSON Web Tokens) for stateless authentication.
- Access tokens have a 30-minute expiry; refresh tokens have a 30-day expiry.
- Tokens are signed with HMAC-SHA256 using a configurable secret.
- Blacklisted tokens are stored in Redis for logout enforcement.

## Consequences

- Stateless API — no server-side session storage needed.
- JWT can be validated without database lookups.
- Token revocation requires Redis (additional infrastructure dependency).
