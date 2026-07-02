# Architecture Guide

## System Context (C4 Level 1)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        JobPilot AI System                           │
│                                                                     │
│  Provides AI-powered career management: resume optimization,        │
│  job discovery, application tracking, interview prep, analytics     │
└──────────┬──────────┬──────────┬──────────┬──────────┬──────────────┘
           │          │          │          │          │
           ▼          ▼          ▼          ▼          ▼
     ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
     │  User   │ │ LinkedIn│ │ OpenAI  │ │ SendGrid│ │ Stripe  │
     │Browser  │ │  Indeed │ │Anthropic│ │  SES    │ │        │
     └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘
```

## Container Architecture (C4 Level 2)

```
┌──────────────────────────────────────────────────────────────────────┐
│                        Web Browser (SPA)                             │
│              Next.js 14 — SSR/ISR/CSR — shadcn/ui                    │
└─────────────────────────────┬────────────────────────────────────────┘
                              │ HTTPS
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    Spring Cloud Gateway (optional)                    │
│                    Rate limit, auth, routing                          │
└─────────────────────────────┬────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                  Core API Server (Modular Monolith)                  │
│                                                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ Identity │ │  Resume  │ │   Job    │ │   App    │ │Interview │  │
│  │  Module  │ │  Module  │ │ Discovery│ │ Tracker  │ │  Module  │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ Company  │ │Analytics │ │  Notif.  │ │   AI     │ │  Admin   │  │
│  │ Intel.   │ │ Module   │ │  Module  │ │ Provider │ │  Module  │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
└──────────┬──────────┬──────────┬──────────┬──────────┬───────────────┘
           │          │          │          │          │
           ▼          ▼          ▼          ▼          ▼
     ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
     │PostgreSQL│ │  Redis   │ │  Kafka   │ │  MinIO   │ │  Ollama  │
     │ +pgvector│ │  Cache   │ │  Events  │ │ Storage  │ │  LocalAI │
     └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
```

## Module Dependency Rules

```
Identity ← Resume ← Job Discovery ← Application ← Interview
    ↓        ↓          ↓              ↓             ↓
  Company ← Analytics ← Notification ← AI Provider ← Admin
```

Dependencies flow one way. Cross-module communication uses domain events via Kafka.
Each module is extractable to a microservice following extraction triggers:
- CPU > 80% sustained for 15 minutes
- Queue depth > 100 unprocessed events
- AI inference latency > 3s P95
- Deployment frequency conflicts

## Communication Patterns

| Pattern | Where | Implementation |
|---------|-------|---------------|
| Synchronous | Same-module calls | Direct service injection |
| Asynchronous | Cross-module | Domain events → Outbox → Kafka |
| Streaming | AI responses | SSE (Server-Sent Events) |
| Real-time | Notifications | WebSocket (STOMP) |
| Batch | Analytics | Scheduled jobs + materialized views |

## AI Provider Architecture

```
Application Layer
       │ uses
       ▼
AiOrchestrationService
  - Provider selection per use case
  - Caching (prompt hash → response)
  - Circuit breaker (50% failure → 30s open)
  - Cost tracking (per-model pricing)
  - Fallback chain on failure
       │ delegates to
       ▼
AIProviderPort (interface)
  - generateText, generateStream, generateEmbedding, countTokens
       ▲
       │ implements
  ┌────┴────┬─────┬──────┐
  │ OpenAI  │Anthropic│Ollama│Gemini│
  │Adapter  │Adapter  │Adapter│Adapter│
  └─────────┴─────────┴──────┴──────┘
```

## Key Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Modular Monolith | 1 deployable JAR | Faster iteration, simpler ops, clear extraction paths |
| PostgreSQL + pgvector | ACID + vector search | Avoid operational complexity of separate vector DB |
| RS256 JWT | Asymmetric signing | Services verify without sharing secret, JWKS rotation |
| Outbox Pattern | Same-DB transaction | Guaranteed delivery without distributed transactions |
| Playwright Java | Browser automation | Modern API, stealth mode, CAPTCHA handling |
| Spring AI | AI abstraction | Native Spring integration, consistent provider interface |
| Kustomize + Kustomize | K8s config | Environment overlays without templating |
