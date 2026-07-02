# JobPilot AI — High Level Design (HLD)

**Version:** 1.0  
**Status:** Draft  
**Phase:** 1 of 35  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Architecture Overview
2. Module Diagram & Bounded Contexts
3. Request Flow
4. Deployment Diagram
5. Technology Decisions & Rationale
6. Communication Patterns
7. Data Flow Architecture
8. Integration Points
9. Security Architecture (High Level)
10. Observability Architecture (High Level)
11. Scaling Boundaries
12. Appendix: C4 Context

---

## 1. Architecture Overview

### 1.1 Architectural Philosophy

JobPilot AI follows **Clean Architecture** (Robert C. Martin) combined with **Domain-Driven Design** tactical patterns within a **Modular Monolith** that can decompose into **Microservices** when scaling demands it.

### 1.2 The Three Principles

| Principle | Application |
|-----------|-------------|
| **Dependency Inversion** | Domain layer depends on nothing. Infrastructure depends on abstractions (ports) defined in the application layer. |
| **Bounded Contexts** | Each module (Resume, Job Discovery, ATS, etc.) owns its data and logic. Communication through domain events. |
| **Strict Layering** | Presentation → Application → Domain → Infrastructure. Never skip layers. |

### 1.3 High-Level System Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │  Next.js Web App │  │ React Native App │  │   Browser Ext.   │  │
│  │  (SSR + CSR)     │  │ (Future Phase)   │  │ (Future Phase)   │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘  │
└───────────┼─────────────────────┼──────────────────────┼────────────┘
            │                     │                      │
            │              HTTPS/WSS (TLS 1.3)           │
            │                     │                      │
┌───────────▼─────────────────────▼──────────────────────▼────────────┐
│                         EDGE LAYER                                  │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Cloudflare CDN (Static assets, caching, DDoS protection)    │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Cloudflare WAF (Rate limiting, bot detection, IP filtering) │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────┬──────────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────────┐
│                      API GATEWAY LAYER                              │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Spring Cloud Gateway                                        │   │
│  │  • TLS termination             • JWT validation              │   │
│  │  • Rate limiting               • Request/response logging    │   │
│  │  • Route mapping               • IP allowlist/blocklist      │   │
│  │  • CORS headers                • Request ID injection        │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────┬──────────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────────┐
│                   APPLICATION LAYER (MODULAR MONOLITH)              │
│                                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │  Auth    │  │  User &  │  │  Resume  │  │   Job    │           │
│  │  Module  │  │  Profile │  │  Studio  │  │ Discovery│           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │  ATS     │  │  Company │  │ Interview│  │  Career  │           │
│  │  Tracker │  │  Intel   │  │   Hub    │  │ Analytics│           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │  Notif.  │  │  Admin   │  │ Settings │  │  Search  │           │
│  │  Service │  │  Portal  │  │  Module  │  │  Engine  │           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  SHARED KERNEL                                                │   │
│  │  • Common domain primitives (Email, Phone, Money, etc.)     │   │
│  │  • Cross-cutting: Security, Auditing, Caching, Logging      │   │
│  │  • Event bus abstraction (in-process + Kafka)              │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────┬──────────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────────┐
│                  INFRASTRUCTURE SERVICES                             │
│                                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │          │  │          │  │          │  │  Browser          │   │
│  │PostgreSQL│  │  Redis   │  │  Kafka   │  │  Automation       │   │
│  │ (Primary)│  │ (Cache)  │  │ (Events) │  │  Engine           │   │
│  └──────────┘  └──────────┘  └──────────┘  │  (Playwright Java) │   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  └──────────────────┘   │
│  │   S3 /   │  │  AI      │  │  Email   │  ┌──────────────────┐   │
│  │  MinIO   │  │ Providers│  │ (SendGrid│  │  Job Aggregator  │   │
│  │ (Files)  │  │(OpenAI,  │  │ /SES)    │  │  (Cron + Workers)│   │
│  └──────────┘  │ Claude)  │  └──────────┘  └──────────────────┘   │
│                └──────────┘                                        │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.4 Layer Responsibilities

| Layer | Responsibility | Technology |
|-------|----------------|------------|
| **Client** | UI rendering, user interaction, client-side state | Next.js, React Native, Chrome Extension |
| **Edge** | CDN, DDoS protection, bot mitigation, WAF | Cloudflare |
| **Gateway** | Routing, JWT validation, rate limiting, CORS, request enrichment | Spring Cloud Gateway |
| **Application** | Business logic, use case orchestration, domain events | Spring Boot (Modular Monolith) |
| **Infrastructure** | Persistence, caching, messaging, external integrations, automation | PostgreSQL, Redis, Kafka, Playwright |

---

## 2. Module Diagram & Bounded Contexts

### 2.1 Bounded Contexts Map

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐   │
│  │   Identity &     │     │   Resume Studio  │     │  Job Discovery  │   │
│  │   Access Context │     │   (Bounded       │     │   (Bounded      │   │
│  │   (Bounded       │◄───►│    Context)      │◄───►│    Context)     │   │
│  │    Context)      │     │                  │     │                 │   │
│  │                  │     │ • Resume Entity  │     │ • JobListing    │   │
│  │ • User Entity    │     │ • ResumeService  │     │ • JobSource     │   │
│  │ • AuthService    │     │ • AtsScore       │     │ • SearchQuery   │   │
│  │ • Role Enum      │     │ • CoverLetter    │     │ • JobMatch      │   │
│  └────────┬─────────┘     └────────┬─────────┘     └────────┬─────────┘   │
│           │                        │                        │            │
│           │                        │                        │            │
│  ┌────────▼─────────┐     ┌────────▼─────────┐     ┌────────▼─────────┐   │
│  │ Application      │     │   Interview Hub   │     │ Company          │   │
│  │ Tracker Context  │◄───►│   (Bounded        │◄───►│ Intelligence     │   │
│  │                  │     │    Context)       │     │ (Bounded         │   │
│  │ • Application    │     │                  │     │  Context)        │   │
│  │ • Pipeline       │     │ • Session        │     │                  │   │
│  │ • KanbanStatus   │     │ • QuestionBank   │     │ • CompanyProfile │   │
│  │ • FollowUp       │     │ • Answer         │     │ • TechStack      │   │
│  └──────────────────┘     └──────────────────┘     └──────────────────┘   │
│                                                                          │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐   │
│  │ Career Analytics │     │  Notification    │     │  Admin Context  │   │
│  │ (Bounded         │     │  Context         │     │                 │   │
│  │  Context)        │◄───►│                  │◄───►│ • SystemConfig  │   │
│  │                  │     │ • Notification   │     │ • UserMgmt      │   │
│  │ • Metric         │     │ • Template       │     │ • AuditLog      │   │
│  │ • Report         │     │ • Channel        │     │ • FeatureFlag   │   │
│  │ • Chart          │     │ • Preference     │     │                 │   │
│  └──────────────────┘     └──────────────────┘     └──────────────────┘   │
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐   │
│  │                     SHARED KERNEL                                   │   │
│  │  • Common: Email, PhoneNumber, Money, Percentage, DateRange       │   │
│  │  • Cross-cutting: SecurityContext, AuditTrail, CacheKey          │   │
│  │  • Events: DomainEvent, EventBus, EventPublisher                 │   │
│  └───────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Context Interaction Rules

| Rule | Description |
|------|-------------|
| **No cyclic dependencies** | Context A may depend on Context B, but B must not depend on A |
| **Anti-corruption layer** | When contexts communicate, use ACL to translate domain models |
| **Domain events** | Side effects across contexts use domain events (not direct calls) |
| **Shared Kernel** | Only stable, generic primitives live here. No business logic. |
| **Context ownership** | Each context owns its persistence. No direct DB access across contexts. |

### 2.3 Module Dependency Graph

```
                    ┌────────────────┐
                    │  Auth Context  │
                    │  (No deps)     │
                    └───────┬────────┘
                            │ depends on
                            ▼
                    ┌────────────────┐
                    │  User/Profile  │
                    │  Context       │
                    └───────┬────────┘
                            │
               ┌───────────┼───────────┐
               │           │           │
               ▼           ▼           ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │  Resume  │ │   Job    │ │ Company  │
        │  Studio  │ │Discovery │ │ Intel    │
        └────┬─────┘ └────┬─────┘ └────┬─────┘
             │            │            │
             ▼            ▼            │
        ┌──────────┐ ┌──────────┐      │
        │   ATS    │ │   Job    │      │
        │ Tracker  │◄┤ Matching │      │
        └────┬─────┘ └────┬─────┘      │
             │            │            │
             ▼            ▼            ▼
        ┌──────────┐ ┌────────────────────┐
        │Interview │ │  Career Analytics  │
        │   Hub    │ │                    │
        └──────────┘ └────────────────────┘
                          │
                          ▼
                  ┌────────────────┐
                  │ Notification   │
                  │ Context        │
                  └────────────────┘
                          ▲
                          │
                  ┌────────────────┐
                  │    Admin       │
                  │    Context     │
                  └────────────────┘
```

---

## 3. Request Flow

### 3.1 Typical Flow: Job Search + Apply

```
USER                          GATEWAY                      APP MODULE                  INFRASTRUCTURE
 │                              │                              │                           │
 │ 1. GET /jobs?q=java&remote   │                              │                           │
 │ ────────────────────────────►│                              │                           │
 │                              │                              │                           │
 │                              │ 2. Validate JWT             │                           │
 │                              │ 3. Extract user_id          │                           │
 │                              │ 4. Check rate limit         │                           │
 │                              │ 5. Inject X-Request-Id      │                           │
 │                              │                              │                           │
 │                              │ 6. GET /api/v1/jobs         │                           │
 │                              │ ───────────────────────────►│                           │
 │                              │                              │                           │
 │                              │                              │ 7. Check Redis cache      │
 │                              │                              │ ────────────────────────►│
 │                              │                              │◄─────────────────────────│
 │                              │                              │   (cache miss)           │
 │                              │                              │                           │
 │                              │                              │ 8. Query PostgreSQL      │
 │                              │                              │    (full-text search)    │
 │                              │                              │ ────────────────────────►│
 │                              │                              │◄─────────────────────────│
 │                              │                              │   (results)              │
 │                              │                              │                           │
 │                              │                              │ 9. Cache results (5 min) │
 │                              │                              │ ────────────────────────►│
 │                              │                              │                           │
 │                              │◄─────────────────────────────│                           │
 │                              │   (paginated results)        │                           │
 │                              │                              │                           │
 │◄─────────────────────────────│                              │                           │
 │  (JSON response)             │                              │                           │
 │                              │                              │                           │
 │ 10. User clicks "Apply"     │                              │                           │
 │ ────────────────────────────►│                              │                           │
 │                              │ 11. POST /api/v1/apps       │                           │
 │                              │ ───────────────────────────►│                           │
 │                              │                              │                           │
 │                              │                              │ 12. Create Application   │
 │                              │                              │     (status: SAVED)      │
 │                              │                              │ ────────────────────────►│
 │                              │                              │                           │
 │                              │                              │ 13. Publish event:       │
 │                              │                              │     ApplicationCreated    │
 │                              │                              │ ────────────────────────►│
 │                              │                              │   (to Kafka/outbox)      │
 │                              │                              │                           │
 │                              │◄─────────────────────────────│                           │
 │                              │  (201 Application resource)  │                           │
 │◄─────────────────────────────│                              │                           │
 │                              │                              │                           │
 │  [Asynchronously]           │                              │                           │
 │ NOTIFICATION SERVICE        │                              │                           │
 │  (consumes event)           │                              │                           │
 │  • Saves notification       │                              │                           │
 │  • Sends email (if opted)   │                              │                           │
 │  • Sends push (if opted)    │                              │                           │
```

### 3.2 Flow: Automated Application (Browser Automation)

```
USER                          GATEWAY                 CORE APP                AUTOMATION SERVICE
 │                              │                        │                         │
 │ POST /apps/{id}/automate     │                        │                         │
 │ ────────────────────────────►│                        │                         │
 │                              │ POST /api/v1/apps      │                         │
 │                              │ ─────────────────────► │                         │
 │                              │                        │                         │
 │                              │                        │ 1. Validate tier (PRO+) │
 │                              │                        │ 2. Check automation limit│
 │                              │                        │ 3. Publish event:        │
 │                              │                        │    AutomationRequested   │
 │                              │                        │ ────────────────────────►│
 │                              │                        │   (via Kafka)            │
 │                              │◄───────────────────────│                         │
 │                              │  (202 Accepted)        │                         │
 │◄─────────────────────────────│                        │                         │
 │                              │                        │                         │
 │                              │                        │  4. Consume event        │
 │                              │                        │  5. Create session       │
 │                              │                        │  6. Launch Playwright    │
 │                              │                        │  7. Navigate to URL      │
 │                              │                        │  8. Detect form fields   │
 │                              │                        │  9. Fill + attach files  │
 │                              │                        │ 10. Handle CAPTCHA (if)  │
 │                              │                        │ 11. Submit               │
 │                              │                        │ 12. Screenshot evidence  │
 │                              │                        │ 13. Update status        │
 │                              │                        │ 14. Publish event:       │
 │                              │                        │     ApplicationSubmitted │
 │                              │                        │                         │
 │  [WS: /topic/automation/{id}] ◄──── progress updates ────                        │
 │                              │                        │                         │
 │  [SSE: /events] ◄───────────│◄─── notification ──────│                         │
```

### 3.3 Flow: AI Operations (Resume Tailoring)

```
USER                   GATEWAY              CORE APP              AI SERVICE            AI PROVIDER
 │                       │                     │                     │                     │
 │ POST /resumes/{id}    │                     │                     │                     │
 │ /tailor?job_id=x      │                     │                     │                     │
 │ ─────────────────────►│                     │                     │                     │
 │                       │                     │                     │                     │
 │                       │ POST /api/v1/...   │                     │                     │
 │                       │ ──────────────────►│                     │                     │
 │                       │                     │                     │                     │
 │                       │                     │ 1. Load resume      │                     │
 │                       │                     │ 2. Load job posting │                     │
 │                       │                     │ 3. Check cache      │                     │
 │                       │                     │    (prompt hash)    │                     │
 │                       │                     │ ───────────────────►│                     │
 │                       │                     │◄────────────────────│                     │
 │                       │                     │   (cache miss)      │                     │
 │                       │                     │                     │                     │
 │                       │                     │ 4. Build prompt     │                     │
 │                       │                     │ 5. Call AI port     │                     │
 │                       │                     │ ───────────────────►│                     │
 │                       │                     │                     │ 6. Choose provider   │
 │                       │                     │                     │ 7. Call OpenAI/Claude│
 │                       │                     │                     │ ───────────────────►│
 │                       │                     │                     │◄────────────────────│
 │                       │                     │                     │   (tailored content) │
 │                       │                     │◄────────────────────│                     │
 │                       │                     │                     │                     │
 │                       │                     │ 8. Cache response   │                     │
 │                       │                     │ 9. Save new version │                     │
 │                       │                     │                     │                     │
 │                       │◄────────────────────│                     │                     │
 │◄──────────────────────│                     │                     │                     │
 │  (200 + tailored      │                     │                     │                     │
 │   resume content)     │                     │                     │                     │
```

### 3.4 Flow: Error Handling (Gateway)

```
CLIENT                     GATEWAY                     APP MODULE
  │                          │                            │
  │ POST /api/v1/resumes     │                            │
  │ ───────────────────────► │                            │
  │                          │                            │
  │                          │ 1. Validate JWT signature  │
  │                          │ 2. Check token expiry      │
  │                          │    ▼ FAIL                  │
  │                          │ JWT expired                │
  │                          │                            │
  │◄─────────────────────────│                            │
  │ 401 + {                  │                            │
  │   error: {               │                            │
  │     code: "TOKEN_EXPIRED",                            │
  │     message: "Access token expired"                   │
  │   },                    │                            │
  │   meta: { request_id } │                            │
  │ }                       │                            │
```

---

## 4. Deployment Diagram

### 4.1 Physical Deployment

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          AWS CLOUD (Primary)                                 │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                     VPC (10.0.0.0/16)                                │   │
│  │                                                                      │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────────────┐   │   │
│  │  │  Public  │  │  Public  │  │  Public  │  │      Public       │   │   │
│  │  │ Subnet A │  │ Subnet B │  │ Subnet C │  │   Subnet (ALB)    │   │   │
│  │  │ us-east- │  │ us-east- │  │ us-east- │  │                   │   │   │
│  │  │   1a     │  │   1b     │  │   1c     │  │  ┌─────────────┐  │   │   │
│  │  │          │  │          │  │          │  │  │  ALB (NLB)  │  │   │   │
│  │  └──────────┘  └──────────┘  └──────────┘  │  │  TLS 1.3    │  │   │   │
│  │                                              │  └─────────────┘  │   │   │
│  │  ┌───────────────────────────────────────────┴──────────────────┐ │   │   │
│  │  │                    Private Subnets                            │ │   │   │
│  │  │                                                              │ │   │   │
│  │  │  ┌─────────────────────────────────────────────────────────┐ │ │   │   │
│  │  │  │  EKS Cluster (Kubernetes 1.30+)                          │ │ │   │   │
│  │  │  │                                                          │ │ │   │   │
│  │  │  │  ┌──────────────────┐  ┌──────────────────┐            │ │ │   │   │
│  │  │  │  │  api-gateway     │  │  core-api        │            │ │ │   │   │
│  │  │  │  │  (2-10 pods)     │  │  (3-20 pods)     │            │ │ │   │   │
│  │  │  │  │  CPU: 1, Mem: 2G │  │  CPU: 2, Mem: 4G │            │ │ │   │   │
│  │  │  │  └──────────────────┘  └──────────────────┘            │ │ │   │   │
│  │  │  │  ┌──────────────────┐  ┌──────────────────┐            │ │ │   │   │
│  │  │  │  │  automation-     │  │  ai-worker       │            │ │ │   │   │
│  │  │  │  │  worker          │  │  (2-10 pods)     │            │ │ │   │   │
│  │  │  │  │  (2-15 pods)     │  │  CPU: 4, Mem: 8G │            │ │ │   │   │
│  │  │  │  │  CPU: 4, Mem: 8G │  └──────────────────┘            │ │ │   │   │
│  │  │  │  └──────────────────┘                                   │ │ │   │   │
│  │  │  │  ┌──────────────────┐  ┌──────────────────┐            │ │ │   │   │
│  │  │  │  │  job-aggregator  │  │  notification-   │            │ │ │   │   │
│  │  │  │  │  (1-5 pods)      │  │  service (2-5)   │            │ │ │   │   │
│  │  │  │  │  CPU: 1, Mem: 2G │  │  CPU: 1, Mem: 2G │            │ │ │   │   │
│  │  │  │  └──────────────────┘  └──────────────────┘            │ │ │   │   │
│  │  │  └─────────────────────────────────────────────────────────┘ │ │   │   │
│  │  │                                                              │ │   │   │
│  │  │  ┌─────────────────────────────────────────────────────────┐ │ │   │   │
│  │  │  │  Data Layer                                              │ │ │   │   │
│  │  │  │                                                          │ │ │   │   │
│  │  │  │  ┌────────────────────┐  ┌────────────────────┐        │ │ │   │   │
│  │  │  │  │  RDS PostgreSQL 16 │  │  ElastiCache       │        │ │ │   │   │
│  │  │  │  │  (Multi-AZ)        │  │  Redis 7 (Cluster) │        │ │ │   │   │
│  │  │  │  │  Primary + 2       │  │  (1 primary + 2)   │        │ │ │   │   │
│  │  │  │  │  Read Replicas     │  │                    │        │ │ │   │   │
│  │  │  │  └────────────────────┘  └────────────────────┘        │ │ │   │   │
│  │  │  │                                                          │ │ │   │   │
│  │  │  │  ┌────────────────────┐  ┌────────────────────┐        │ │ │   │   │
│  │  │  │  │  MSK (Kafka)       │  │  S3 Bucket         │        │ │ │   │   │
│  │  │  │  │  (3 brokers)       │  │  (Resumes,         │        │ │ │   │   │
│  │  │  │  │                    │  │   Screenshots,     │        │ │ │   │   │
│  │  │  │  │                    │  │   Exports)         │        │ │ │   │   │
│  │  │  │  └────────────────────┘  └────────────────────┘        │ │ │   │   │
│  │  │  └─────────────────────────────────────────────────────────┘ │ │   │   │
│  │  └──────────────────────────────────────────────────────────────┘ │   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  External Services                                                    │   │
│  │                                                                      │   │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐   │   │
│  │  │OpenAI   │  │Anthropic│  │ SendGrid│  │  Stripe │  │LinkedIn │   │   │
│  │  │  API    │  │  API    │  │  (Email) │  │  (Pay)  │  │  OAuth  │   │   │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────┘   │   │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐                │   │
│  │  │Indeed   │  │Glassdoor│  │ Google  │  │ Job     │                │   │
│  │  │  (Jobs) │  │ (Jobs)  │  │  Jobs   │  │ Boards  │                │   │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘                │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Container Architecture (Docker per Service)

```
┌──────────────────────────────────────────┐
│           core-api:latest                 │
│  ┌────────────────────────────────────┐   │
│  │  JVM: eclipse-temurin:21-jre       │   │
│  │  Alpine (distroless for prod)      │   │
│  │                                    │   │
│  │  Port: 8080                        │   │
│  │  Heap: -Xms512m -Xmx2g             │   │
│  │  GC: ZGC (low latency)            │   │
│  │  DNS: JNDI + inet address caching │   │
│  │                                    │   │
│  │  Health: /actuator/health          │   │
│  │  Liveness: /actuator/health/liveness│  │
│  │  Readiness: /actuator/health/ready  │   │
│  └────────────────────────────────────┘   │
└──────────────────────────────────────────┘
```

### 4.3 Kubernetes Deploy Strategy

```yaml
# Conceptual structure (not YAML code)
core-api-deployment:
  replicas: 3 (minimum) → HPA to 20
  resources:
    requests: 1 CPU, 2GB RAM
    limits: 2 CPU, 4GB RAM
  strategy: RollingUpdate (maxSurge: 1, maxUnavailable: 0)
  affinity: pod anti-affinity (spread across AZs)
  envFrom: configMapRef + secretRef

automation-worker-deployment:
  replicas: 2 (minimum) → HPA to 15
  resources:
    requests: 4 CPU, 4GB RAM
    limits: 6 CPU, 8GB RAM
  strategy: RollingUpdate (maxSurge: 1, maxUnavailable: 1)
  affinity: prefer separate nodes (resource heavy)
```

---

## 5. Technology Decisions & Rationale

### 5.1 Decision Records

| ID | Decision | Rejected Alternatives | Rationale |
|----|----------|----------------------|-----------|
| **TD-1** | **Modular Monolith** (Phase 1) | Full Microservices | Faster iteration, simpler deployment, single transaction boundary. Extract services only when metrics prove a bottleneck. |
| **TD-2** | **Java 21 + Spring Boot 3** | Kotlin, Go, Python | Team skill alignment, virtual threads, records, sealed classes. Spring ecosystem unmatched for enterprise Java. |
| **TD-3** | **PostgreSQL 16** | MySQL, CockroachDB, DynamoDB | pgvector for embeddings, JSONB for flexible schemas, mature full-text search, CTEs. Avoids multi-DB complexity. |
| **TD-4** | **Redis 7** | Memcached, Hazelcast | Data structures (sorted sets for rate limiting, streams for task queues), built-in clustering, pub/sub for WebSocket. |
| **TD-5** | **Kafka** | RabbitMQ, SQS, Pulsar | Durable message replay, partitioning for scale, schema registry. Critical for reliable automation job queue. |
| **TD-6** | **Playwright Java** | Selenium, Puppeteer | Modern API, browser context isolation, better CAPTCHA handling, Chrome DevTools Protocol. |
| **TD-7** | **Spring AI** | LangChain4j, custom wrapper | Consistent abstraction across LLM providers, Spring-native, tool/function calling support. |
| **TD-8** | **Next.js 14** | Vite+React, Remix, SPA | SSR for SEO, ISR for job pages, React Server Components, App Router conventions. |
| **TD-9** | **EKS (Kubernetes)** | ECS, Fargate, Lambda | Portability, self-healing, auto-scaling, rich ecosystem. K8s is standard for serious deployments. |
| **TD-10** | **pgvector** | Pinecone, Weaviate, Qdrant | Avoid another DB. Native PostgreSQL integration, transactional consistency with job data. |
| **TD-11** | **Testcontainers** | Embedded DB, H2 | Real PostgreSQL/Redis/Kafka in tests. Catches integration bugs that embedded DBs miss. |
| **TD-12** | **ArgoCD (GitOps)** | Jenkins, Spinnaker | Declarative, state reconciliation, audit trail. Aligns with infra-as-code philosophy. |
| **TD-13** | **Structured JSON Logging** | Plain text logs | Machine-parseable, searchable in Loki/ELK, structured context per event. |
| **TD-14** | **OpenTelemetry** | Zipkin, Jaeger alone | Vendor-neutral, supports tracing + metrics + logs correlation. Future-proof. |

### 5.2 Why NOT These

| Technology | Why Not |
|------------|---------|
| **Kotlin** | Team Java expertise, Spring Boot Java support is first-class, records bridge the verbosity gap |
| **MongoDB** | ACID required for financial data (subscriptions), relational queries needed for ATS reporting |
| **Serverless (Lambda)** | Cold start kills AI/automation latency, 15-min timeout insufficient for browser automation |
| **gRPC (primary)** | REST is universal, browser/mobile-friendly. gRPC reserved for inter-service high-throughput paths later |
| **WebFlux (everywhere)** | Virtual threads make reactive unnecessary for most endpoints. WebFlux only for high-I/O streaming paths |
| **Custom auth** | Spring Security + OAuth2 is battle-tested, covers 99% of requirements out of the box |

---

## 6. Communication Patterns

### 6.1 Synchronous Communication (REST)

| Pattern | Use Case | Details |
|---------|----------|---------|
| Request-Response | CRUD operations (users, jobs, applications) | Standard REST JSON. All reads go through API Gateway. |
| Paginated Query | Search results, listings | Cursor-based pagination (opaque cursor, not page numbers) |
| Optimistic Locking | Resume editing, concurrent updates | `@Version` in JPA entities, 409 Conflict on stale data |

### 6.2 Asynchronous Communication (Events)

| Event | Publisher | Consumer(s) | Trigger |
|-------|-----------|-------------|---------|
| `UserRegistered` | Auth Module | Notification (welcome email), Analytics (new user metric) | Registration complete |
| `ApplicationCreated` | ATS Module | Notification (confirmation), Analytics (funnel update) | User saves/applies to job |
| `ApplicationStatusChanged` | ATS Module | Notification, Automation (if rejected → new search) | Pipeline status update |
| `AutomationRequested` | ATS Module | Automation Service | User initiates auto-apply |
| `AutomationCompleted` | Automation Service | ATS Module (status update), Notification | Auto-apply finishes |
| `AutomationFailed` | Automation Service | ATS Module (status update), Notification | Auto-apply error |
| `ResumeTailored` | AI Service | Resume Module (new version) | AI tailoring completes |
| `InterviewSessionCompleted` | Interview Hub | Analytics (score tracking), Career (progress update) | Mock interview ends |
| `SubscriptionChanged` | Billing Module | Auth Module (role update), Notification | Plan change/payment |

### 6.3 Outbox Pattern

To guarantee event delivery:

```
1. Application service starts DB transaction
2. Writes domain event to outbox table (same DB)
3. Commits transaction
4. Outbox poller picks up unprocessed events (Transaction Outbox pattern)
5. Publishes to Kafka
6. Marks event as processed
```

### 6.4 Real-Time Communication (WebSocket)

| Path | Purpose | Direction |
|------|---------|-----------|
| `/ws/notifications/{userId}` | Push notifications | Server → Client |
| `/ws/automation/{sessionId}` | Automation progress streaming | Server → Client |
| `/ws/interviews/{sessionId}` | Live interview session | Bidirectional |

WebSocket uses STOMP over SockJS with fallback.

---

## 7. Data Flow Architecture

### 7.1 User Registration Flow

```
Email/Password → Gateway → AuthModule → Validate → Hash bcrypt → Save User → 
  → Create Subscription (Free) → Publish UserRegistered → 
    → Notification: Welcome Email
    → Analytics: Increment signup metric
```

### 7.2 Job Application Flow

```
User clicks Apply → ATS Module → Create Application (SAVED) → 
  → Publish ApplicationCreated →
    → Notification: Confirmation to user
    → Analytics: Update funnel

User initiates auto-apply → ATS Module → Validate tier → Publish AutomationRequested →
  → Automation Service consumed event →
    → Fetch job URL → Playwright launch → Form detect → Fill →
      → Submit → Screenshot → Publish AutomationCompleted →
        → ATS Module: Update status (APPLIED)
        → Notification: "Application submitted successfully"
```

### 7.3 AI Resume Tailoring Flow

```
User requests tailor → Resume Module → Load resume + job description →
  → Build prompt (from prompt templates) → Call AI Port →
    → AI Service: Select provider → Build context → Call LLM →
      → Return tailored content → Resume Module: Save new version →
        → Return to user
```

### 7.4 Job Aggregation Flow

```
Scheduler triggers → Job Aggregator → For each active source adapter:
  → Fetch new/updated listings (paginated)
  → Transform to canonical JobListing model
  → Deduplicate against existing (source_id index)
  → For new listings: Generate embedding (pgvector)
  → Save batch to database
  → For each user with matching saved search:
    → Publish JobAlert event
      → Notification: Push/email new matches
```

---

## 8. Integration Points

### 8.1 External Integrations

| Integration | Protocol | Authentication | Rate Limit | Fallback |
|-------------|----------|----------------|------------|----------|
| **OpenAI API** | HTTPS REST | API Key (Bearer) | Tier-dependent (check headers) | Fallback to Claude |
| **Anthropic API** | HTTPS REST | API Key (x-api-key) | Tier-dependent | Fallback to GPT-3.5 |
| **Stripe** | HTTPS REST + Webhook | API Key (Bearer) | None practical | Retry with backoff |
| **SendGrid / SES** | HTTPS REST (SMTP) | API Key | ~100/hr per source | Queue + retry |
| **LinkedIn OAuth** | HTTPS REST | OAuth 2.0 | Standard provider limits | Google OAuth as backup |
| **Google OAuth** | HTTPS REST | OAuth 2.0 | Standard | GitHub OAuth as backup |
| **Indeed API** | HTTPS REST | API Key | ~1000/day | Scrape fallback |
| **Glassdoor/Other** | HTTPS REST | API Key | Varies | Scrape fallback |

### 8.2 Integration Security

- All API keys stored in **HashiCorp Vault / AWS Secrets Manager**
- Secrets injected as environment variables or Spring Cloud Config
- Webhook endpoints validated by signature verification (Stripe HMAC)
- Outbound traffic filtered through NAT Gateway with allowlist

---

## 9. Security Architecture (High Level)

### 9.1 Defense in Depth Layers

```
Layer 1: Cloudflare WAF — DDoS, bot mitigation, IP blocklist
Layer 2: API Gateway — JWT validation, rate limiting, CORS
Layer 3: Spring Security — Method-level @PreAuthorize, role checks
Layer 4: Input Validation — @Valid, OWASP sanitization
Layer 5: Database — Row-level security, encryption at rest
Layer 6: Audit — All sensitive operations logged
```

### 9.2 Token Architecture

```
Access Token (JWT):
  - Claims: sub (user_id), role, email, iat, exp
  - Signed: RS256 (asymmetric)
  - Expiry: 15 minutes
  - Transport: httpOnly, Secure, SameSite=Strict cookie

Refresh Token:
  - Opaque UUID (not JWT)
  - Expiry: 7 days
  - Stored: Redis (hashed), tracks revocation
  - Rotation: Old refresh token invalidated on use

Token Flow:
  Login → JWT (15m) + Refresh (7d) → API calls with JWT →
  JWT expires → POST /auth/refresh → New JWT + Rotated Refresh →
  Refresh expires → Re-login / OAuth re-auth
```

### 9.3 RBAC Matrix

| Feature | FREE | PRO | ENTERPRISE | ADMIN |
|---------|------|-----|------------|-------|
| Resume Builder | ✓ | ✓ | ✓ | — |
| Cover Letter (AI) | ✗ | ✓ | ✓ | — |
| Auto-Apply | ✗ | ✓ | ✓ | — |
| Interview Practice | 5/mo | Unlimited | Unlimited | — |
| Career Analytics | Basic | Advanced | Full | — |
| Saved Searches | 3 | 50 | Unlimited | — |
| Team Accounts | ✗ | ✗ | ✓ | — |
| Admin Panel | ✗ | ✗ | ✗ | ✓ |

---

## 10. Observability Architecture (High Level)

### 10.1 The Three Pillars

```
                   ┌─────────────────────────────┐
                   │      APPLICATION            │
                   │  Structured JSON Logging    │
                   │  (Logback → stdout → Loki) │
                   └─────────────┬───────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   METRICS       │  │      LOGS        │  │     TRACES      │
│   Prometheus    │  │   Loki / ELK     │  │  OpenTelemetry  │
│   + Grafana     │  │   30-day retention│  │  + Jaeger       │
│                 │  │                 │  │                 │
│ • HTTP requests │  │ • ERROR level   │  │ • Request-level  │
│ • Latency (p95) │  │ • WARN level    │  │ • Service graph  │
│ • Error rates   │  │ • Audit events  │  │ • Bottleneck ID  │
│ • Queue depth   │  │ • Business events│  │ • Latency breakdown │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 10.2 Health Checks

| Endpoint | Purpose | Expected |
|----------|---------|----------|
| `/actuator/health` | Aggregate health | UP |
| `/actuator/health/liveness` | K8s liveness probe | UP |
| `/actuator/health/readiness` | K8s readiness probe | UP |
| `/actuator/health/db` | Database connectivity | UP |
| `/actuator/health/redis` | Redis connectivity | UP |
| `/actuator/health/kafka` | Kafka connectivity | UP |
| `/actuator/health/ai` | AI provider reachability | UP (degraded OK) |
| `/actuator/info` | Build / commit / version info | JSON |

### 10.3 Alert Channels

- **Critical:** PagerDuty + Slack #incidents
- **Warning:** Slack #alerts
- **Info:** Email digest

---

## 11. Scaling Boundaries

### 11.1 When to Extract Microservices

| Service | Extraction Trigger | Separated By |
|---------|-------------------|--------------|
| **Browser Automation** | CPU usage > 80% consistently, or concurrent sessions > 50 | Kafka events + REST |
| **AI Orchestration** | AI request queue > 1000 pending, or cost tracking complexity | Kafka + REST |
| **Job Aggregation** | Scraping schedule conflicts with core API resources | Kafka events |
| **Notification** | Email volume > 10k/day, or need independent failover | Kafka events |

### 11.2 Modular Monolith Extraction Strategy

```
Phase 1: All in one JAR
  com.jobpilot.modules.{module}/
    - domain/
    - application/
    - infrastructure/
    - interfaces/

Phase 2: Extracted as separate Maven module (same repo)
  jobpilot-core/
  jobpilot-automation/
  jobpilot-ai/
  jobpilot-gateway/

Phase 3: Separate deployable (different repos)
  jobpilot-core-service/
  jobpilot-automation-service/
  jobpilot-ai-service/
  jobpilot-gateway/
```

### 11.3 Database Scaling Thresholds

| Metric | Threshold | Action |
|--------|-----------|--------|
| Connections | > 150 of 200 pool | Add read replica, increase pool |
| Query latency (p95) | > 100ms | Add index, cache, or vertical scale |
| Table size (job_listings) | > 10M rows | Partition by month |
| Table size (audit_log) | > 50M rows | Archive to S3 Parquet |
| Write throughput | > 5000 TPS | Shard by user_id hash |
| Embedding query time | > 200ms | Reduce IVF lists, add RAM |

---

## 12. Appendix: C4 Context (High Level)

### Level 1: System Context

```
┌─────────────────────────────────────────────────────┐
│  [Person] Job Seeker                                 │
│  ┌───────────────────────────────────────────────┐  │
│  │  Uses JobPilot AI to manage their job search, │  │
│  │  optimize resumes, auto-apply, and prepare   │  │
│  │  for interviews                               │  │
│  └───────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
              ┌────────────────┐
              │   JobPilot AI  │
              │   [System]     │
              └────────┬───────┘
                       │
              ┌────────┴────────┐
              │                 │
              ▼                 ▼
   ┌────────────────┐  ┌────────────────┐
   │ AI Providers   │  │ Job Boards     │
   │ [Ext System]   │  │ [Ext System]   │
   └────────────────┘  └────────────────┘
   ┌────────────────┐  ┌────────────────┐
   │ Stripe         │  │ Email Service  │
   │ [Ext System]   │  │ [Ext System]   │
   └────────────────┘  └────────────────┘
```

---

*This HLD serves as the architectural blueprint for Phase 2 onward. Every subsequent phase (LLD, Database Design, C4) must conform to the patterns, decisions, and constraints defined here.*

---

**End of HLD v1.0**
