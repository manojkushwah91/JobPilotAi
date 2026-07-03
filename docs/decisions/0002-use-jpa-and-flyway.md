# 2. Use JPA with Flyway for Database Migrations

**Date:** 2026-07-03

## Status

Accepted

## Context

The application needs a relational database with schema versioning to manage changes across development, staging, and production environments.

## Decision

- Use **JPA (Hibernate)** as the ORM for object-relational mapping.
- Use **Flyway** for database migration versioning.
- Migrations are stored under `db/migration/dev/` for the development profile.
- Naming convention: `V<number>__<description>.sql`.

## Consequences

- Schema changes are repeatable and auditable.
- Migrations run automatically on application startup.
- JPA entity changes must be accompanied by corresponding Flyway migrations.
