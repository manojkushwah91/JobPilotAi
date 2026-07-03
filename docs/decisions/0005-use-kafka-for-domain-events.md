# 5. Use Kafka for Domain Events

**Date:** 2026-07-03

## Status

Proposed

## Context

The system has several asynchronous concerns: sending welcome emails after registration, notifying users of application status changes, and triggering AI scoring workflows. Direct synchronous calls create coupling and degrade API response times.

## Decision

Use **Apache Kafka** (with `spring-kafka`) for publishing and consuming domain events. Topics are defined per aggregate type:
- `jobpilot.user.events`
- `jobpilot.application.events`
- `jobpilot.notification.events`

## Consequences

- Decouples event producers from consumers.
- Enables future event sourcing and audit log capabilities.
- Adds operational complexity: Kafka cluster management and consumer offset tracking.
- Event schema evolution must be managed.
