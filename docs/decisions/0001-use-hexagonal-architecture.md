# 1. Use Hexagonal Architecture (Ports & Adapters)

**Date:** 2026-07-03

## Status

Accepted

## Context

The JobPilot application needs to be resilient to infrastructure changes. Core business logic should not depend on frameworks, databases, or external services. The system interacts with PostgreSQL, Redis, Kafka, email providers, file storage, and AI models.

## Decision

Use hexagonal architecture (ports and adapters):
- **Domain layer** (`jobpilot-domain`): pure Java with no framework dependencies, contains entities and value objects.
- **Application layer** (`jobpilot-application`): use cases/application services, defines `Port` interfaces for external interactions.
- **Infrastructure layer** (`jobpilot-infrastructure`): `Adapter` implementations for all ports (JPA repositories, Playwright, email, file storage, AI clients).
- **Interfaces layer** (`jobpilot-interfaces`): REST controllers, DTOs, and security configuration.

Dependencies point inward: Interfaces → Application → Domain. Infrastructure adapts application ports.

## Consequences

- Core business logic is testable without infrastructure.
- Swapping databases or external services requires only new adapter implementations.
- Increased initial project structure complexity.
