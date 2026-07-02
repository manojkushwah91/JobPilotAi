# JobPilot AI — C4 Architecture

**Version:** 1.0  
**Status:** Draft  
**Phase:** 3 of 35  
**Author:** Chief Software Architect  

---

## Table of Contents

1. C4 Level 1 — System Context Diagram
2. C4 Level 2 — Container Diagram
3. C4 Level 3 — Component Diagram (per container)
4. C4 Level 4 — Code Diagram (key aggregates)

---

## 1. C4 Level 1 — System Context Diagram

**Scope:** JobPilot AI Platform  
**Primary Audience:** Technical and non-technical stakeholders  

### 1.1 Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              EXTERNAL SYSTEMS                               │
│                                                                              │
│  ┌─────────────────────────────┐    ┌─────────────────────────────┐        │
│  │     [Person] Job Seeker     │    │     [Person] Admin          │        │
│  │  Primary user who manages   │    │  Platform administrator who │        │
│  │  job search, optimizes      │    │  manages users, config,    │        │
│  │  resumes, auto-applies,     │    │  and monitors health.      │        │
│  │  and prepares interviews.   │    │                             │        │
│  └──────────────┬──────────────┘    └──────────────┬──────────────┘        │
│                 │                                  │                        │
│                 │ Uses [HTTPS/WSS]                  │ Uses [HTTPS]           │
│                 ▼                                  ▼                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   JobPilot AI [Software System]                     │   │
│  │  "The AI Career Operating System"                                  │   │
│  │  Allows job seekers to manage their entire career journey —        │   │
│  │  from job discovery and AI-powered resume optimization to          │   │
│  │  automated applications and interview preparation.                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                 │                                  │                        │
│                 │ Uses [HTTPS REST/WebSocket]      │                        │
│                 ▼                                  │                        │
│  ┌─────────────────────────────┐    ┌──────────────┴──────────────┐        │
│  │  AI Providers [Ext System]  │    │  Job Boards [Ext System]    │        │
│  │  OpenAI, Anthropic, Ollama  │    │  LinkedIn, Indeed, Glassdoor│        │
│  │  Google Gemini             │    │  Google Jobs, Company Sites │        │
│  │  Provides LLM capabilities  │    │  Provides job listings      │        │
│  └─────────────────────────────┘    └─────────────────────────────┘        │
│                                                                              │
│  ┌─────────────────────────────┐    ┌─────────────────────────────┐        │
│  │  Stripe [Ext System]        │    │  SendGrid / AWS SES         │        │
│  │  Payment processing and     │    │  Email delivery for         │        │
│  │  subscription management.   │    │  notifications and digests. │        │
│  └─────────────────────────────┘    └─────────────────────────────┘        │
│                                                                              │
│  ┌─────────────────────────────┐    ┌─────────────────────────────┐        │
│  │  Google/LinkedIn/GitHub     │    │  Cloudflare [Ext System]    │        │
│  │  OAuth Providers            │    │  CDN, WAF, DDoS protection │        │
│  │  Social login               │    │  DNS, bot management       │        │
│  └─────────────────────────────┘    └─────────────────────────────┘        │
│                                                                              │
│  ┌─────────────────────────────┐                                           │
│  │  Amazon S3 [Ext System]     │                                           │
│  │  File storage for resumes,  │                                           │
│  │  cover letters, screenshots │                                           │
│  └─────────────────────────────┘                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Context Description

| Element | Type | Description |
|---------|------|-------------|
| **Job Seeker** | Person | Primary user who registers, searches jobs, creates resumes, auto-applies, practices interviews |
| **Admin** | Person | Internal platform administrator who manages users, system config, monitors health |
| **JobPilot AI** | Software System | The platform — manages career lifecycle with AI, automation, and tracking |
| **AI Providers** | External System | LLM APIs for resume tailoring, cover letters, interview scoring, career advice |
| **Job Boards** | External System | Sources of job listings — LinkedIn, Indeed, Glassdoor, Google Jobs, company career pages |
| **Stripe** | External System | Processes subscription payments, manages billing, invoices |
| **SendGrid / SES** | External System | Sends transactional emails (welcome, confirmations, digests, alerts) |
| **OAuth Providers** | External System | Social login via Google, LinkedIn, GitHub, Microsoft |
| **Cloudflare** | External System | CDN for static assets, WAF for security, DDoS protection, DNS |
| **Amazon S3** | External System | Object storage for resume PDFs, cover letters, automation screenshots, exports |

### 1.3 Relationships

| From | To | Description | Technology |
|------|----|-------------|------------|
| Job Seeker | JobPilot AI | Browses jobs, manages applications, uses AI features | HTTPS, WebSocket |
| Admin | JobPilot AI | Manages users, config, views analytics | HTTPS |
| JobPilot AI | AI Providers | Sends prompts for resume tailoring, interview questions, scoring | HTTPS REST |
| JobPilot AI | Job Boards | Fetches job listings, company info | HTTPS (scraping/API) |
| JobPilot AI | Stripe | Creates checkout sessions, handles webhooks | HTTPS REST |
| JobPilot AI | SendGrid/SES | Sends emails (welcome, alerts, digests) | HTTPS REST/SMTP |
| JobPilot AI | OAuth Providers | Initiates OAuth 2.0 flows | HTTPS |
| JobPilot AI | Cloudflare | Serves static assets, passes through API traffic | HTTPS |
| JobPilot AI | S3 | Stores/retrieves user files | HTTPS REST (AWS SDK) |

---

## 2. C4 Level 2 — Container Diagram

**Scope:** JobPilot AI Platform  
**Primary Audience:** Software architects, developers, DevOps  

### 2.1 Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                          JobPilot AI — Container Diagram                             │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  [Container: Web Application]  Next.js 14 + TypeScript + Tailwind + shadcn/ui│   │
│  │  Delivers static content (SSR/ISR) from CDN. Handles client-side rendering. │   │
│  │  Communicates with API Gateway via HTTPS/WSS.                               │   │
│  │  Technology: Node.js 20, React 18, Next.js 14, React Query, Zustand         │   │
│  └────────────────────────────────────┬─────────────────────────────────────────┘   │
│                                       │                                              │
│  ┌────────────────────────────────────▼─────────────────────────────────────────┐   │
│  │  [Container: API Gateway]  Spring Cloud Gateway                               │   │
│  │  Single entry point for all client requests. Handles auth validation,        │   │
│  │  rate limiting, routing, CORS, request enrichment, and response aggregation. │   │
│  │  Technology: Java 21, Spring Cloud Gateway 4.x, Reactive (WebFlux)           │   │
│  └────────────────────────────────────┬─────────────────────────────────────────┘   │
│                                       │                                              │
│  ┌────────────────────────────────────▼─────────────────────────────────────────┐   │
│  │  [Container: Core API Server]  Spring Boot 3 + Clean Architecture            │   │
│  │  Modular monolith hosting all business logic across bounded contexts.         │   │
│  │  Exposes REST APIs consumed by the web app and mobile apps.                  │   │
│  │  Technology: Java 21, Spring Boot 3, Spring Security, Spring Data JPA,       │   │
│  │               Spring Data Redis, Spring Kafka, Spring AI                      │   │
│  └──────┬──────────────────────┬───────────────────────┬─────────────────────────┘   │
│         │                      │                       │                              │
│         │ JDBC (HikariCP)      │ Jedis/Lettuce          │ Kafka Producer/Consumer      │
│         ▼                      ▼                       ▼                              │
│  ┌──────────────┐    ┌──────────────────┐    ┌──────────────────────┐                │
│  │ [Container:   │    │ [Container:      │    │ [Container:         │                │
│  │  PostgreSQL]  │    │  Redis]          │    │  Kafka]             │                │
│  │  16.x         │    │  7.2             │    │  3.7                │                │
│  │  Primary DB   │    │  Cache +         │    │  Event Bus          │                │
│  │  + pgvector   │    │  Session Store   │    │  + Outbox           │                │
│  │  + read rep   │    │  + Rate Limit    │    │  + Schema Registry  │                │
│  └──────────────┘    └──────────────────┘    └──────────────────────┘                │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  [Container: Browser Automation Worker]  Playwright Java                      │   │
│  │  Separate deployable service that consumes automation requests from Kafka,    │   │
│  │  controls headless Chrome browsers, fills and submits job applications.       │   │
│  │  Technology: Java 21, Playwright Java 1.45+, Kafka Consumer                   │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  [Container: AI Orchestration Worker]  Spring Boot 3                          │   │
│  │  Separate deployable service that handles AI requests asynchronously.         │   │
│  │  Manages provider selection, prompt resolution, token tracking, caching.     │   │
│  │  Technology: Java 21, Spring AI, Kafka Consumer, Redis Cache                 │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  [Container: Job Aggregation Worker]  Spring Boot 3                           │   │
│  │  Scheduled service that fetches job listings from external sources,           │   │
│  │  deduplicates, generates embeddings, and stores in PostgreSQL.                │   │
│  │  Technology: Java 21, Spring Scheduler, Jsoup/RestTemplate, pgvector         │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  [Container: S3 / MinIO]  Object Storage                                       │   │
│  │  Stores resume PDFs, cover letters, automation screenshots, exported files.    │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Container Descriptions

| # | Container | Technology | Responsibilities | Scaling |
|---|-----------|------------|------------------|---------|
| 1 | **Web Application** | Next.js 14, TypeScript, Tailwind, shadcn/ui | SSR for SEO, CSR for interactive features, WebSocket for real-time updates, static asset serving via CDN | Horizontal (CDN + serverless) |
| 2 | **API Gateway** | Spring Cloud Gateway 4.x, Java 21, WebFlux | JWT validation, rate limiting, route to services, CORS, request ID injection, response aggregation | Horizontal (stateless) |
| 3 | **Core API Server** | Spring Boot 3, Java 21, Clean Architecture | All business logic (auth, users, resumes, applications, interviews, analytics, admin, settings) | Horizontal (stateless) |
| 4 | **PostgreSQL** | PostgreSQL 16 + pgvector | Primary database, job search (full-text via tsvector), semantic search (pgvector), read replicas for queries | Vertical + read replicas |
| 5 | **Redis** | Redis 7.2 | Cache (job listings, AI responses, user sessions), rate limiting (sorted sets), WebSocket pub/sub | Cluster mode |
| 6 | **Kafka** | Apache Kafka 3.7 + Schema Registry | Event bus for domain events, automation job queue, AI request queue, notification queue | Partition scaling |
| 7 | **Browser Automation Worker** | Playwright Java 1.45+, Java 21 | Consumes automation requests, runs headless Chrome, detects/submits application forms | Horizontal (CPU-bound) |
| 8 | **AI Orchestration Worker** | Spring AI, Java 21 | Consumes AI requests, routes to providers, manages prompts, tracks tokens/cost | Horizontal (I/O-bound) |
| 9 | **Job Aggregation Worker** | Spring Boot 3, Jsoup, pgvector | Scheduled job fetching, deduplication, embedding generation, saved search matching | Horizontal (scheduled) |
| 10 | **S3 / MinIO** | AWS S3 or self-hosted MinIO | Resume files, cover letters, automation screenshots, exports, profile photos | Managed |

### 2.3 Inter-Container Communication

| From Container | To Container | Protocol | Purpose |
|----------------|-------------|----------|---------|
| Web App | API Gateway | HTTPS/WSS | All API calls, WebSocket connections |
| API Gateway | Core API Server | HTTPS (internal) | Routed API requests |
| API Gateway | Redis | RESP | JWT blacklist, rate limit checks |
| Core API Server | PostgreSQL | JDBC (TLS) | All data persistence |
| Core API Server | Redis | RESP | Caching, session store, rate limiting |
| Core API Server | Kafka | Kafka Protocol | Publish domain events |
| Core API Server | S3 | HTTPS (AWS SDK) | File upload/download |
| Auto Worker | Kafka | Kafka Protocol | Consume automation requests, publish results |
| AI Worker | Kafka | Kafka Protocol | Consume AI requests, publish results |
| AI Worker | Redis | RESP | Cache AI responses, store token usage |
| AI Worker | AI Providers | HTTPS REST | LLM API calls |
| Auto Worker | External Job Sites | HTTPS | Browser automation navigation |
| Job Aggregator | External Job Sites | HTTPS | Fetch job listings |
| Job Aggregator | PostgreSQL | JDBC (TLS) | Store job listings |
| Job Aggregator | Kafka | Kafka Protocol | Publish new job events |

---

## 3. C4 Level 3 — Component Diagram

**Scope:** Core API Server  
**Primary Audience:** Software architects, developers  

### 3.1 Core API Server — Top Level Components

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  [Container: Core API Server]  Spring Boot 3 + Clean Architecture                    │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  PRESENTATION LAYER (interfaces)                                             │   │
│  │                                                                              │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐  │   │
│  │  │  Auth    │ │  User    │ │  Resume  │ │  App     │ │  GlobalException │  │   │
│  │  │  REST    │ │  REST    │ │  REST    │ │  REST    │ │  Handler         │  │   │
│  │  │  Controller │  Controller│  Controller│  Controller│                  │  │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────────────────┘  │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                        │   │
│  │  │  Interview│ │  Career  │ │  Admin   │ │  Job     │                        │   │
│  │  │  REST    │ │  REST    │ │  REST    │ │  REST    │                        │   │
│  │  │  Controller│  Controller│  Controller│  Controller│                        │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘                        │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                        │   │
│  │  │  Settings │ │  Notif.  │ │  Search  │ │ WebSocket│                        │   │
│  │  │  REST    │ │  REST    │ │  REST    │ │  Handler │                        │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘                        │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                       │                                              │
│                                       ▼                                              │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  APPLICATION LAYER (application)                                            │   │
│  │                                                                              │   │
│  │  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐ │   │
│  │  │ AuthAppService │ │ UserAppService │ │ ResumeApp      │ │ AppTracker   │ │   │
│  │  │                │ │                │ │ Service        │ │ AppService   │ │   │
│  │  │ RegisterUC     │ │ GetProfileUC   │ │ CreateUC       │ │ CreateUC     │ │   │
│  │  │ LoginUC        │ │ UpdateBasicUC  │ │ TailorUC       │ │ StatusChange │ │   │
│  │  │ OAuthUC        │ │ AddSkillUC     │ │ ScoreUC        │ │ AutomationUC │ │   │
│  │  │ RefreshUC      │ │ AddExpUC       │ │ ExportUC       │ │ FollowUpUC   │ │   │
│  │  └───────┬────────┘ └───────┬────────┘ └───────┬────────┘ └──────┬───────┘ │   │
│  │  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐ │   │
│  │  │ Interview App  │ │ Career Analytics│ │ AdminAppService│ │JobDiscovery  │ │   │
│  │  │ Service        │ │ AppService     │ │                │ │AppService    │ │   │
│  │  │ CreateSession  │ │ GetDashboard   │ │ ListUsers      │ │ SearchUC     │ │   │
│  │  │ SubmitAnswer   │ │ GetFunnel      │ │ SuspendUser    │ │ SaveSearchUC │ │   │
│  │  │ CompleteSession│ │ WeeklyDigest   │ │ FeatureFlags   │ │ MatchUC      │ │   │
│  │  └───────┬────────┘ └───────┬────────┘ └───────┬────────┘ └──────┬───────┘ │   │
│  │  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐ │   │
│  │  │AtsOptimizer    │ │CoverLetter     │ │ Notification   │ │ PromptEngine │ │   │
│  │  │Service         │ │Service         │ │ Service        │ │ Service      │ │   │
│  │  └────────────────┘ └────────────────┘ └────────────────┘ └──────────────┘ │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                       │                                              │
│                                       ▼                                              │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  DOMAIN LAYER (domain)                                                      │   │
│  │                                                                              │   │
│  │  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐ │   │
│  │  │ Identity &     │ │ Resume Studio  │ │  Application   │ │ Job Discovery│ │   │
│  │  │ Access         │ │ Domain         │ │  Tracker       │ │ Domain       │ │   │
│  │  │                │ │                │ │  Domain        │ │              │ │   │
│  │  │ User (entity)  │ │ Resume (AR)    │ │ Application(AR)│ │ JobListing   │ │   │
│  │  │ Email (VO)     │ │ ResumeVersion  │ │ StatusChange   │ │ SearchQuery  │ │   │
│  │  │ UserId (VO)    │ │ AtsScore (VO)  │ │ ApplicationNote│ │ SalaryRange  │ │   │
│  │  │ Role (enum)    │ │ ResumeSection  │ │ TimelineEvent  │ │ JobSource    │ │   │
│  │  │ OAuthProvider  │ │ CoverLetter   │ │ AutomationInfo  │ │ SavedSearch  │ │   │
│  │  └────────┬───────┘ └────────┬───────┘ └────────┬───────┘ └──────┬────────┘ │   │
│  │  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌──────────────┐ │   │
│  │  │ Interview Hub  │ │   Company      │ │  Career        │ │ AI Provider  │ │   │
│  │  │ Domain         │ │   Intel Domain │ │  Analytics     │ │ Domain       │ │   │
│  │  │                │ │                │ │  Domain        │ │              │ │   │
│  │  │ InterviewSess. │ │ CompanyProfile │ │ AnalyticsAggr. │ │ AIProvider   │ │   │
│  │  │ InterviewQuest │ │ SalaryDataPoint│ │ FunnelStage    │ │ (port intf)  │ │   │
│  │  │ QuestionScore  │ │ FundingRound   │ │ DataPoint      │ │ AiRequest    │ │   │
│  │  │ SessionFeedback│ │ HiringTrends   │ │ MetricValue    │ │ AiResponse   │ │   │
│  │  └────────────────┘ └────────────────┘ └────────────────┘ └──────────────┘ │   │
│  │                                                                              │   │
│  │  ┌────────────────────────────────────────────────────────────────────┐     │   │
│  │  │  SHARED KERNEL                                                     │     │   │
│  │  │  Email, PhoneNumber, Money, Percentage, DateRange, Address,        │     │   │
│  │  │  Url, FileRef, Duration, Language, Theme, Timezone                 │     │   │
│  │  └────────────────────────────────────────────────────────────────────┘     │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                       │                                              │
│                                       ▼                                              │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │  INFRASTRUCTURE LAYER (infrastructure)                                       │   │
│  │                                                                              │   │
│  │  ┌────────────────────────────┐ ┌────────────────────────────┐              │   │
│  │  │  Persistence               │ │  Messaging                 │              │   │
│  │  │  UserJpaRepository         │ │  KafkaEventPublisher       │              │   │
│  │  │  ResumeJpaRepository       │ │  KafkaEventConsumer        │              │   │
│  │  │  ApplicationJpaRepository  │ │  OutboxPoller              │              │   │
│  │  │  JobListingJpaRepository   │ │  AutomationEventProducer   │              │   │
│  │  │  CompanyJpaRepository      │ │  NotificationEventConsumer │              │   │
│  │  │  FlywayMigration           │ └────────────────────────────┘              │   │
│  │  └────────────────────────────┘                                              │   │
│  │  ┌────────────────────────────┐ ┌────────────────────────────┐              │   │
│  │  │  External Integrations     │ │  Caching                   │              │   │
│  │  │  JwtTokenProvider          │ │  RedisCacheService         │              │   │
│  │  │  BCryptPasswordEncoder     │ │  RedisRateLimiter          │              │   │
│  │  │  GoogleOAuthClient         │ │  RedisSessionStore         │              │   │
│  │  │  LinkedInOAuthClient       │ │  CacheAsideHelper          │              │   │
│  │  │  SendGridEmailSender       │ └────────────────────────────┘              │   │
│  │  │  S3FileStorage             │                                              │   │
│  │  │  StripePaymentGateway      │ ┌────────────────────────────┐              │   │
│  │  └────────────────────────────┘ │  AI Provider Adapters      │              │   │
│  │  ┌────────────────────────────┐ │  OpenAiAdapter             │              │   │
│  │  │  Scheduling                │ │  AnthropicAdapter          │              │   │
│  │  │  FollowUpScheduler         │ │  OllamaAdapter             │              │   │
│  │  │  JobAggregationScheduler   │ │  GeminiAdapter             │              │   │
│  │  │  DigestScheduler           │ │  CircuitBreakerAspect      │              │   │
│  │  │  AnalyticsCalculator       │ └────────────────────────────┘              │   │
│  │  └────────────────────────────┘                                              │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Component: Auth Module (Detailed)

```
┌──────────────────────────────────────────────────────────────────────────┐
│  AUTH MODULE                                                             │
│                                                                          │
│  interfaces                                                              │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AuthController                                                    │   │
│  │  POST /register    → authAppService.register(command)            │   │
│  │  POST /login       → authAppService.authenticate(command)        │   │
│  │  POST /oauth/{p}   → authAppService.oauthLogin(command)          │   │
│  │  POST /refresh     → authAppService.refreshToken(command)        │   │
│  │  POST /logout      → authAppService.logout(command)              │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  application                                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AuthApplicationService                                           │   │
│  │                                                                    │   │
│  │ register(command) {                                               │   │
│  │   1. userPort.existsByEmail(command.email()) → throw if exists   │   │
│  │   2. hash = passwordEncoder.encode(command.password())            │   │
│  │   3. user = User.create(command.email(), hash, command.name())     │   │
│  │   4. saved = userPort.save(user)                                  │   │
│  │   5. eventPublisher.publish(new UserRegisteredEvent(saved.id()))  │   │
│  │   6. accessToken = tokenProvider.generateAccessToken(saved)       │   │
│  │   7. refreshToken = tokenProvider.generateRefreshToken(saved)     │   │
│  │   8. return AuthResponse(accessToken, refreshToken)               │   │
│  │ }                                                                  │   │
│  │                                                                    │   │
│  │ authenticate(command) { ... }                                     │   │
│  │ oauthLogin(command) { ... }                                       │   │
│  │ refreshToken(command) { ... }                                     │   │
│  │ logout(command) { ... }                                           │   │
│  │ forgotPassword(command) { ... }                                   │   │
│  │ resetPassword(command) { ... }                                    │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  Used Ports (interfaces):                                                │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ <<interface>> UserRepository          (port.outbound)            │   │
│  │  findByEmail(Email): Optional<User>                             │   │
│  │  findById(UserId): Optional<User>                                │   │
│  │  save(User): User                                                │   │
│  │  existsByEmail(Email): boolean                                   │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ <<interface>> TokenProvider           (port.outbound)            │   │
│  │  generateAccessToken(User): String                               │   │
│  │  generateRefreshToken(User): String                              │   │
│  │  validateAccessToken(String): TokenClaims                        │   │
│  │  validateRefreshToken(String, UserId): boolean                   │   │
│  │  invalidateRefreshToken(String): void                            │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ <<interface>> PasswordEncoder         (port.outbound)            │   │
│  │  encode(CharSequence): String                                    │   │
│  │  matches(CharSequence, String): boolean                          │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ <<interface>> EventPublisher          (port.outbound)            │   │
│  │  publish(DomainEvent): void                                      │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  domain                                                                  │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ User (entity, aggregate root)                                    │   │
│  │  - id: UserId (UUID)                                            │   │
│  │  - email: Email (value object)                                   │   │
│  │  - passwordHash: PasswordHash (value object)                     │   │
│  │  - role: Role (enum: FREE, PRO, ENTERPRISE, ADMIN)               │   │
│  │  - oauthProviders: Set<OAuthProvider>                            │   │
│  │  - emailVerifiedAt: Instant                                      │   │
│  │  - deletedAt: Instant (soft delete)                              │   │
│  │  - createdAt, updatedAt: Instant                                 │   │
│  │                                                                    │   │
│  │  + static create(email, passwordHash, fullName): User            │   │
│  │  + verifyEmail(): void                                            │   │
│  │  + updateRole(Role): void                                         │   │
│  │  + softDelete(): void                                             │   │
│  │  + addOAuthProvider(OAuthProvider): void                          │   │
│  │  + isEnabled(): boolean                                           │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ Domain Events:                                                    │   │
│  │  UserRegisteredEvent(userId, email, role, occurredAt)            │   │
│  │  UserVerifiedEvent(userId, occurredAt)                           │   │
│  │  UserDeletedEvent(userId, occurredAt)                            │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ Domain Exceptions:                                                │   │
│  │  EmailAlreadyExistsException                                     │   │
│  │  InvalidEmailException                                           │   │
│  │  WeakPasswordException                                           │   │
│  │  UserNotFoundException                                           │   │
│  │  EmailNotVerifiedException                                       │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  infrastructure                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ UserJpaRepository extends JpaRepository<UserEntity, UUID>       │   │
│  │  Optional<UserEntity> findByEmail(String email)                  │   │
│  │  boolean existsByEmail(String email)                             │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ UserRepositoryImpl implements UserRepository                     │   │
│  │  - UserMapper (MapStruct): UserEntity ↔ User                    │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ JwtTokenProvider implements TokenProvider                        │   │
│  │  - RS256 signing (KeyPair loaded from Vault)                    │   │
│  │  - Access: 15 min expiry                                        │   │
│  │  - Refresh: opaque UUID, stored in Redis (hashed)               │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ BCryptPasswordEncoder implements PasswordEncoder                 │   │
│  │  - Strength: 12 rounds                                          │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ GoogleOAuthClient implements OAuthClientPort                     │   │
│  │ LinkedInOAuthClient implements OAuthClientPort                   │   │
│  │ GitHubOAuthClient implements OAuthClientPort                     │   │
│  │ MicrosoftOAuthClient implements OAuthClientPort                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.3 Component: Browser Automation Worker (Detailed)

```
┌──────────────────────────────────────────────────────────────────────────┐
│  BROWSER AUTOMATION WORKER                                               │
│                                                                          │
│  entry                                                                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AutomationKafkaConsumer (Kafka @KafkaListener)                  │   │
│  │  consumes "automation-requests" topic                           │   │
│  │  for each message: orchestrator.execute(message)                │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  core                                                                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AutomationOrchestrator                                           │   │
│  │                                                                    │   │
│  │ execute(AutomationRequest) {                                      │   │
│  │   session = sessionRepo.create(request)  // state: QUEUED         │   │
│  │   try {                                                            │   │
│  │     session.transitionTo(State.INITIALIZING)                      │   │
│  │     emitProgress(session, "Starting browser...")                  │   │
│  │                                                                    │   │
│  │     proxy = proxyManager.getNextProxy()                            │   │
│  │     browser = browserFactory.create(session.id(), proxy)           │   │
│  │                                                                    │   │
│  │     session.transitionTo(State.NAVIGATING)                        │   │
│  │     browser.navigate(session.jobUrl())                             │   │
│  │     emitProgress(session, "Navigated to application page")        │   │
│  │                                                                    │   │
│  │     if (browser.hasCaptcha()) {                                    │   │
│  │       session.transitionTo(State.BLOCKED)                         │   │
│  │       session.markManualRequired("CAPTCHA detected")               │   │
│  │       emitComplete(session, "MANUAL_REQUIRED")                    │   │
│  │       return                                                       │   │
│  │     }                                                              │   │
│  │                                                                    │   │
│  │     session.transitionTo(State.FORM_DETECT)                       │   │
│  │     fields = browser.detectFormFields()                            │   │
│  │     mappedFields = formMapper.mapFields(fields, userProfile)       │   │
│  │     emitProgress(session, "Detected ${fields.size()} form fields") │   │
│  │                                                                    │   │
│  │     session.transitionTo(State.FORM_FILL)                         │   │
│  │     for ((selector, value) : mappedFields) {                       │   │
│  │       browser.fillField(selector, value)                          │   │
│  │       Thread.sleep(randomDelay())  // human-like                  │   │
│  │     }                                                              │   │
│  │     browser.uploadFile(resumeSelector, resumeFileUrl)              │   │
│  │     emitProgress(session, "Form filled with profile data")         │   │
│  │                                                                    │   │
│  │     session.transitionTo(State.SUBMIT)                            │   │
│  │     preScreenshot = browser.takeScreenshot()                       │   │
│  │     browser.click(submitSelector)                                  │   │
│  │     browser.waitForNavigation()                                    │   │
│  │     postScreenshot = browser.takeScreenshot()                      │   │
│  │     emitProgress(session, "Application submitted")                 │   │
│  │                                                                    │   │
│  │     session.transitionTo(State.VERIFY)                            │   │
│  │     confirmationText = browser.getConfirmationText()               │   │
│  │                                                                    │   │
│  │     evidence = AutomationEvidence(preScreenshot, postScreenshot,   │   │
│  │                                  confirmationText)                │   │
│  │     evidenceUrl = fileStorage.upload(session.id(), evidence)       │   │
│  │                                                                    │   │
│  │     session.transitionTo(State.COMPLETED)                         │   │
│  │     session.attachEvidence(evidenceUrl)                            │   │
│  │     sessionRepo.save(session)                                      │   │
│  │                                                                    │   │
│  │     eventPublisher.publish(                                        │   │
│  │       AutomationCompletedEvent(session.id(), "SUBMITTED"))         │   │
│  │     emitComplete(session, "SUCCESS")                               │   │
│  │                                                                    │   │
│  │   } catch (RetryableException e) {                                 │   │
│  │     if (session.attemptCount() < session.maxRetries()) {          │   │
│  │       session.incrementAttempt()                                   │   │
│  │       session.transitionTo(State.RETRYING)                         │   │
│  │       eventPublisher.publish(                                      │   │
│  │         AutomationRetryEvent(session.id(), e.message()))           │   │
│  │       // Re-enqueue with delay                                     │   │
│  │       kafkaTemplate.send("automation-retry", session.id())        │   │
│  │     } else {                                                       │   │
│  │       session.transitionTo(State.FAILED)                           │   │
│  │       session.recordError(e.message())                             │   │
│  │       eventPublisher.publish(                                      │   │
│  │         AutomationFailedEvent(session.id(), e.message()))          │   │
│  │       emitComplete(session, "FAILED")                              │   │
│  │     }                                                              │   │
│  │   } catch (FatalException e) {                                     │   │
│  │     session.transitionTo(State.FAILED)                             │   │
│  │     session.recordError(e.message())                               │   │
│  │     eventPublisher.publish(                                        │   │
│  │       AutomationFailedEvent(session.id(), e.message()))            │   │
│  │     emitComplete(session, "FAILED")                                │   │
│  │   } finally {                                                      │   │
│  │     browser.close()                                                │   │
│  │   }                                                                │   │
│  │ }                                                                   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  components                                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ <<interface>> BrowserInstancePort  (port.outbound)              │   │
│  │  + createSession(url, proxy, ua): BrowserSessionId             │   │
│  │  + navigate(url): NavigationResult                              │   │
│  │  + detectFormFields(): List<DetectedFormField>                  │   │
│  │  + fillField(selector, value): void                             │   │
│  │  + uploadFile(selector, fileUrl): void                          │   │
│  │  + click(selector): void                                        │   │
│  │  + selectOption(selector, value): void                         │   │
│  │  + takeScreenshot(): byte[]                                      │   │
│  │  + getPageText(): String                                         │   │
│  │  + hasCaptcha(): boolean                                         │   │
│  │  + getConfirmationText(): String                                 │   │
│  │  + closeSession(): void                                          │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ PlaywrightBrowserEngine implements BrowserInstancePort          │   │
│  │  - Browser, BrowserContext, Page management                     │   │
│  │  - Anti-detection: user-agent, viewport, typing speed, mouse   │   │
│  │  - Form detection: DOM analysis, label-field association        │   │
│  │  - CAPTCHA detection: image analysis heuristic                  │   │
│  │  - Resource cleanup on close                                    │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ <<interface>> ProxyManagerPort   (port.outbound)                │   │
│  │  + getNextProxy(): ProxyConfig                                  │   │
│  │  + markBad(proxy): void                                         │   │
│  │  + markGood(proxy): void                                        │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ SimpleProxyManager implements ProxyManagerPort                  │   │
│  │  - Round-robin rotation                                         │   │
│  │  - Bad proxy tracking (moving window)                           │   │
│  │  - Integration: BrightData/Oxylabs residential pool            │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ <<interface>> FormMapperPort      (port.outbound)               │   │
│  │  + mapFields(fields, profile): Map<String, String>             │   │
│  │  + identifyAtsPlatform(dom): AtsPlatformType                   │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ AtsFormDetector implements FormMapperPort                       │   │
│  │  - GreenhouseAdapter, LeverAdapter, WorkdayAdapter              │   │
│  │  - GenericAdapter (fallback — heuristic)                        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  infrastructure                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AutomationSessionRepository (JPA)                               │   │
│  │  - AutomationSessionEntity                                       │   │
│  │  - State machine transitions persisted                           │   │
│  │  - Evidence stored as JSONB                                      │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ KafkaEventPublisher (publishes completion events)                │   │
│  │ FileStoragePort (S3 SDK — uploads screenshots)                   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.4 Component: AI Orchestration Worker (Detailed)

```
┌──────────────────────────────────────────────────────────────────────────┐
│  AI ORCHESTRATION WORKER                                                 │
│                                                                          │
│  entry                                                                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AiKafkaConsumer (@KafkaListener)                                │   │
│  │  consumes "ai-requests" topic                                   │   │
│  │  for each: orchestrator.process(request)                        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  core                                                                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ AiOrchestrationService                                          │   │
│  │                                                                    │   │
│  │ process(AiTaskRequest) {                                          │   │
│  │   cacheKey = buildCacheKey(request)                               │   │
│  │   cached = cacheService.get(cacheKey)                             │   │
│  │   if (cached.isPresent()) return cached.get()                     │   │
│  │                                                                    │   │
│  │   provider = providerSelector.select(                             │   │
│  │     request.useCase(),                                            │   │
│  │     request.preferredModel(),                                     │   │
│  │     request.requirements())                                       │   │
│  │                                                                    │   │
│  │   if (!tokenTracker.checkLimit(request.userId(), request))        │   │
│  │     throw new AiQuotaExceededException()                          │   │
│  │                                                                    │   │
│  │   wrappedRequest = AiRequest(                                     │   │
│  │     provider, request.messages(),                                 │   │
│  │     request.temperature(),                                         │   │
│  │     request.maxTokens())                                          │   │
│  │                                                                    │   │
│  │   response = providerAdapter.generateText(wrappedRequest)         │   │
│  │                                                                    │   │
│  │   cacheService.set(cacheKey, response, request.cacheTtl())        │   │
│  │   tokenTracker.recordUsage(request.userId(), response.usage())    │   │
│  │                                                                    │   │
│  │   return response                                                 │   │
│  │ }                                                                  │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ ProviderSelector (strategy)                                      │   │
│  │  select(useCase, preferred, requirements): AiProviderType        │   │
│  │  - Priority: preferred → cheapest capable → fallback             │   │
│  │  - Circuit breaker check (skip if OPEN)                         │   │
│  │  - Availability check (health pings)                            │   │
│  │  - Model-to-useCase mapping:                                     │   │
│  │     RESUME_TAILORING → GPT-4 / Claude 3 Opus                     │   │
│  │     RESUME_SCORING → GPT-4 / Claude 3 Sonnet                    │   │
│  │     COVER_LETTER → GPT-4 / Claude 3 Sonnet                      │   │
│  │     INTERVIEW_PREDICT → GPT-4 / Claude 3 Sonnet                 │   │
│  │     ANSWER_SCORING → GPT-4 / Claude 3 Haiku (fast)             │   │
│  │     CAREER_ADVICE → GPT-3.5 / Claude 3 Haiku (cost efficient)  │   │
│  │     NETWORKING_MSG → GPT-3.5 / Claude 3 Haiku                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  ports (interfaces)                                                      │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ <<interface>> AIProviderPort  (port.outbound)                   │   │
│  │  + generateText(AiRequest): AiResponse                          │   │
│  │  + generateStream(AiRequest): Flux<AiChunk>                     │   │
│  │  + generateEmbedding(String): List<Float>                       │   │
│  │  + countTokens(String): int                                     │   │
│  │                                                                    │   │
│  │ Domain objects:                                                   │   │
│  │  AiRequest, AiResponse, AiMessage, AiChunk                        │   │
│  │  TokenUsage, AiModel, AiProviderType                              │   │
│  │  FinishReason, ResponseFormat                                     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  infrastructure — adapters                                               │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ OpenAiAdapter implements AIProviderPort                          │   │
│  │  - Spring AI OpenAiChatClient (or direct HttpClient)            │   │
│  │  - Endpoint: https://api.openai.com/v1                          │   │
│  │  - Models: gpt-4, gpt-4-turbo, gpt-3.5-turbo                   │   │
│  │  - Embeddings: text-embedding-3-small (512d) or 3-large (1536d) │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ AnthropicAdapter implements AIProviderPort                       │   │
│  │  - Spring AI AnthropicChatClient (or direct HttpClient)         │   │
│  │  - Endpoint: https://api.anthropic.com/v1                        │   │
│  │  - Models: claude-3-opus, claude-3-sonnet, claude-3-haiku       │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ OllamaAdapter implements AIProviderPort                          │   │
│  │  - Spring AI OllamaChatClient                                    │   │
│  │  - Local endpoint: http://localhost:11434                        │   │
│  │  - Models: llama3, mixtral, codellama                           │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ GeminiAdapter implements AIProviderPort                          │   │
│  │  - Google Vertex AI / Gemini API                                 │   │
│  │  - Models: gemini-1.5-pro, gemini-1.5-flash                     │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │ CircuitBreakerAspect (AOP)                                      │   │
│  │  - Wraps all AIProviderPort calls                                │   │
│  │  - States: CLOSED (pass), OPEN (fail fast), HALF_OPEN (test)   │   │
│  │  - Failure threshold: 5 in 60s → OPEN                           │   │
│  │  - Timeout: 30s per call                                        │   │
│  │  - Fallback: log error, throw AiServiceUnavailableException     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 4. C4 Level 4 — Code Diagram

**Scope:** Key aggregates within Core API Server  
**Primary Audience:** Developers implementing the system  

### 4.1 Code Diagram: Resume Aggregate

```
┌──────────────────────────────────────────────────────────────────────┐
│  RESUME AGGREGATE (Domain Layer)                                    │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ Resume (Aggregate Root)                                     │     │
│  │                                                            │     │
│  │  - id: ResumeId                           [UUID wrapper]    │     │
│  │  - userId: UserId                         [UUID wrapper]    │     │
│  │  - title: ResumeTitle                     [String wrapper]   │     │
│  │  - templateId: TemplateId                 [UUID wrapper]    │     │
│  │  - sections: List<ResumeSection>          [ordered list]     │     │
│  │  - versions: List<ResumeVersion>          [immutable log]    │     │
│  │  - currentVersion: ResumeVersion          [reference]       │     │
│  │  - atsScores: List<AtsScore>              [tracking history] │     │
│  │  - createdAt: Instant                     [set once]        │     │
│  │  - updatedAt: Instant                     [updated on change]│     │
│  │  - deletedAt: Instant?                    [soft delete]     │     │
│  │                                                            │     │
│  │  + static createFromProfile(UserProfile, ResumeTitle): Resume   │
│  │  + addSection(ResumeSection): void                          │     │
│  │  + updateSection(SectionId, ResumeSection): void             │     │
│  │  + removeSection(SectionId): void                           │     │
│  │  + reorderSections(List<SectionId>): void                   │     │
│  │  + changeTemplate(TemplateId): void                         │     │
│  │  + createVersion(label): ResumeVersion                      │     │
│  │  + restoreVersion(versionNumber): void                      │     │
│  │  + calculateAtsScore(JobDescription): AtsScore              │     │
│  │  + tailorToJob(JobDescription, config): Resume              │     │
│  │  + softDelete(): void                                       │     │
│  │                                                            │     │
│  │  Invariants:                                                │     │
│  │   • sections.size() >= 1 (min: summary + skills)            │     │
│  │   • versions is never empty (at least 1 version)            │     │
│  │   • Only one version is active at a time                    │     │
│  │   • section.order is unique within sections                 │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ contains                                        │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ ResumeSection (Value Object, stored as JSONB)               │     │
│  │                                                            │     │
│  │  - id: SectionId                           [UUID]          │     │
│  │  - type: SectionType                       [enum]          │     │
│  │  - title: String                            [display name]  │     │
│  │  - content: StructuredContent               [polymorphic]  │     │
│  │  - order: int                               [position]     │     │
│  │                                                            │     │
│  │  SectionType:                                                │     │
│  │    SUMMARY, EXPERIENCE, EDUCATION, SKILLS,                  │     │
│  │    CERTIFICATIONS, PROJECTS, LANGUAGES, CUSTOM             │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ is a                                            │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ StructuredContent (sealed interface)                        │     │
│  │                                                            │     │
│  │  ├── TextContent(sections: List<String>)                   │     │
│  │  ├── ExperienceContent(experiences: List<ExpItem>)         │     │
│  │  ├── EducationContent(educations: List<EduItem>)          │     │
│  │  └── SkillContent(skills: List<SkillItem>)                │     │
│  │                                                            │     │
│  │  ExpItem: role, company, location, startDate, endDate,     │     │
│  │           current, achievements[], technologies[]          │     │
│  │  EduItem: institution, degree, field, start, end, gpa     │     │
│  │  SkillItem: name, proficiency, years                       │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ referenced by                                   │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ ResumeVersion (Value Object)                                │     │
│  │                                                            │     │
│  │  - versionNumber: int                       [incrementing] │     │
│  │  - label: String                            [user-friendly] │     │
│  │  - contentSnapshot: ResumeContent           [frozen copy]   │     │
│  │  - createdAt: Instant                                      │     │
│  │  - isActive: boolean                        [current]      │     │
│  │                                                            │     │
│  │  ResumeContent = structured JSON snapshot of all sections   │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ references                                      │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ AtsScore (Value Object)                                    │     │
│  │                                                            │     │
│  │  - score: int (0-100)                    [overall]         │     │
│  │  - sectionScores: Map<SectionType, int>  [per section]     │     │
│  │  - keywordMatches: Map<String, int>      [word → count]    │     │
│  │  - missingKeywords: List<String>         [gaps]            │     │
│  │  - suggestions: List<AtsSuggestion>      [improvements]    │     │
│  │  - keywordDensity: double                [naturalness]     │     │
│  │  - formatScore: int                      [parsability]     │     │
│  │  - formattingIssues: List<FormatIssue>   [problems]        │     │
│  │  - analyzedAt: Instant                   [timestamp]       │     │
│  │  - jobDescriptionHash: String            [dedup]           │     │
│  │                                                            │     │
│  │  AtsSuggestion(category, severity, message, section, actions)  │
│  │  FormatIssue(type, description, location)                      │
│  └────────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.2 Code Diagram: Application Aggregate (ATS Pipeline)

```
┌──────────────────────────────────────────────────────────────────────┐
│  APPLICATION AGGREGATE (Domain Layer)                                │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ Application (Aggregate Root)                               │     │
│  │                                                            │     │
│  │  - id: ApplicationId                     [UUID]            │     │
│  │  - userId: UserId                        [owner FK]        │     │
│  │  - jobListingId: JobListingId            [job FK]          │     │
│  │  - resumeId: ResumeId?                   [used resume]     │     │
│  │  - coverLetterId: CoverLetterId?         [optional]        │     │
│  │  - status: ApplicationStatus             [state machine]   │     │
│  │  - statusHistory: List<StatusChange>     [immutable log]   │     │
│  │  - automationInfo: AutomationInfo?        [optional]       │     │
│  │  - notes: List<ApplicationNote>          [entities]        │     │
│  │  - attachments: List<ApplicationAttachment> [entities]     │     │
│  │  - timeline: List<TimelineEvent>          [ordered log]    │     │
│  │  - followUp: FollowUp?                    [reminder]       │     │
│  │  - salaryOffered: SalaryRange?            [offer info]     │     │
│  │  - appliedAt: Instant?                    [when applied]   │     │
│  │  - createdAt, updatedAt: Instant                           │     │
│  │                                                            │     │
│  │  + static create(userId, jobId): Application              │     │
│  │  + transitionTo(ApplicationStatus, changedBy, note): void │     │
│  │  + addNote(content, category): ApplicationNote            │     │
│  │  + removeNote(noteId): void                               │     │
│  │  + addAttachment(fileName, url, type): ApplicationAttach  │     │
│  │  + removeAttachment(attachmentId): void                    │     │
│  │  + setAutomation(info: AutomationInfo): void              │     │
│  │  + setFollowUp(dueDate, type, notes): void                │     │
│  │  + completeFollowUp(): void                               │     │
│  │  + updateSalaryOffered(SalaryRange): void                 │     │
│  │  + addTimelineEvent(TimelineEvent): void                   │     │
│  │  + softDelete(): void                                      │     │
│  │                                                            │     │
│  │  Invariants:                                                │     │
│  │   • Status transitions must follow state machine rules      │     │
│  │   • Cannot transition to same status                       │     │
│  │   • Deleted applications reject all mutations             │     │
│  │   • Only one automation session per application            │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ has state machine                                │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ ApplicationStatus (enum with transition validation)         │     │
│  │                                                            │     │
│  │  SAVED → APPLIED → PHONE_SCREEN → TECHNICAL_INTERVIEW →    │     │
│  │           ONSITE_INTERVIEW → OFFER → ACCEPTED              │     │
│  │                                      ↘ REJECTED (any)     │     │
│  │                                      ↘ WITHDRAWN (any)    │     │
│  │                                                            │     │
│  │  canTransitionTo(from, to): boolean                        │     │
│  │    - SAVED: APPLIED, WITHDRAWN                               │     │
│  │    - APPLIED: PHONE_SCREEN, REJECTED, WITHDRAWN              │     │
│  │    - PHONE_SCREEN: TECHNICAL_INTERVIEW, REJECTED            │     │
│  │    - TECHNICAL_INTERVIEW: ONSITE_INTERVIEW, REJECTED        │     │
│  │    - ONSITE_INTERVIEW: OFFER, REJECTED                      │     │
│  │    - OFFER: ACCEPTED, REJECTED, WITHDRAWN                    │     │
│  │    - ACCEPTED: (terminal)                                    │     │
│  │    - REJECTED: (terminal)                                    │     │
│  │    - WITHDRAWN: (terminal)                                   │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ value objects                                   │
│                    ▼                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐                │
│  │ StatusChange (VO)    │  │ AutomationInfo (VO)  │                │
│  │  - from: Application │  │  - status: AutoState │                │
│  │    Status            │  │  - sessionId: UUID   │                │
│  │  - to: Application   │  │  - submittedAt: Inst │                │
│  │    Status            │  │  - evidenceUrl: URL  │                │
│  │  - changedBy: String │  │  - formData: Map     │                │
│  │  - note: String?     │  │  - errorMessage: Str │                │
│  │  - timestamp: Inst   │  │  - attemptCount: int │                │
│  └──────────────────────┘  └──────────────────────┘                │
│                    │                                                 │
│                    │ entities                                        │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ ApplicationNote (Entity)                                   │     │
│  │  - id: NoteId                  [UUID]                      │     │
│  │  - content: String             [note text]                 │     │
│  │  - category: NoteCategory      [enum]                      │     │
│  │  - createdAt, updatedAt: Instant                           │     │
│  │                                                            │     │
│  │  NoteCategory: GENERAL, PREP, FOLLOW_UP, RESEARCH, OFFER  │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ TimelineEvent (Value Object, ordered)                      │     │
│  │  - id: EventId                  [UUID]                     │     │
│  │  - type: TimelineEventType      [enum]                     │     │
│  │  - title: String                [human readable]           │     │
│  │  - description: String?         [details]                  │     │
│  │  - timestamp: Instant           [when it happened]         │     │
│  │  - metadata: Map<String, String> [links, refs, IDs]       │     │
│  │                                                            │     │
│  │  TimelineEventType: APPLICATION_SUBMITTED, STATUS_CHANGED, │     │
│  │    NOTE_ADDED, INTERVIEW_SCHEDULED, FOLLOW_UP_SET,         │     │
│  │    OFFER_RECEIVED, AUTOMATION_STARTED, AUTOMATION_COMPLETE,│     │
│  │    EMAIL_RECEIVED, RESEARCH_ADDED                           │     │
│  └────────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.3 Code Diagram: Browser Automation Session

```
┌──────────────────────────────────────────────────────────────────────┐
│  AUTOMATION SESSION (Domain Layer — separate module)                 │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ AutomationSession (Aggregate Root)                         │     │
│  │                                                            │     │
│  │  - id: AutomationSessionId               [UUID]            │     │
│  │  - userId: UserId                        [owner FK]        │     │
│  │  - applicationId: ApplicationId          [parent FK]       │     │
│  │  - state: SessionState                   [state machine]   │     │
│  │  - jobUrl: URL                            [target URL]      │     │
│  │  - atsPlatform: AtsPlatformType?          [detected]        │     │
│  │  - formFields: List<DetectedFormField>    [detected fields] │     │
│  │  - submittedData: Map<String, String>    [what was sent]    │     │
│  │  - attemptCount: int                      [retry counter]   │     │
│  │  - maxRetries: int                        [config, def: 3]  │     │
│  │  - proxyUsed: String?                    [proxy IP]         │     │
│  │  - userAgentUsed: String?                [UA string]        │     │
│  │  - evidence: AutomationEvidence?          [screenshots]     │     │
│  │  - errorMessage: String?                  [failure reason]  │     │
│  │  - startedAt: Instant                     [when queued]     │     │
│  │  - completedAt: Instant?                  [when done]       │     │
│  │  - createdAt, updatedAt: Instant                           │     │
│  │                                                            │     │
│  │  + static create(applicationId, userId, jobUrl): AutoSess  │     │
│  │  + transitionTo(SessionState): void       [validate + set] │     │
│  │  + setDetectedFields(fields): void                         │     │
│  │  + setSubmittedData(Map<String, String>): void            │     │
│  │  + attachEvidence(AutomationEvidence): void                │     │
│  │  + recordError(message): void                              │     │
│  │  + incrementAttempt(): void                                │     │
│  │  + markManualRequired(reason): void                        │     │
│  │  + isRetryable(): boolean                                   │     │
│  │  + isComplete(): boolean                                    │     │
│  │                                                            │     │
│  │  Invariants:                                               │     │
│  │   • State transitions follow SessionState machine rules    │     │
│  │   • attemptCount <= maxRetries (enforced by orchestrator)  │     │
│  │   • Evidence only set on COMPLETED or FAILED               │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ state machine                                   │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ SessionState (enum)                                        │     │
│  │                                                            │     │
│  │  ┌─────────┐                                                │     │
│  │  │ QUEUED  │                                                │     │
│  │  └────┬────┘                                                │     │
│  │       ▼                                                     │     │
│  │  ┌────────────┐                                             │     │
│  │  │INITIALIZING│                                             │     │
│  │  └────┬───────┘                                             │     │
│  │       ▼                                                     │     │
│  │  ┌───────────┐                                              │     │
│  │  │NAVIGATING │                                              │     │
│  │  └─────┬─────┘                                              │     │
│  │        │ ┌──────────┐  ┌───────────────────┐               │     │
│  │        ├─► CAPTCHA  ├──► MANUAL_REQUIRED    │               │     │
│  │        │ └──────────┘  └───────────────────┘               │     │
│  │        ▼                                                   │     │
│  │  ┌────────────┐                                            │     │
│  │  │FORM_DETECT │                                            │     │
│  │  └─────┬──────┘                                            │     │
│  │        ▼                                                   │     │
│  │  ┌──────────┐                                              │     │
│  │  │FORM_FILL │                                              │     │
│  │  └────┬─────┘                                              │     │
│  │       ▼                                                    │     │
│  │  ┌────────┐  ┌────────────┐                                │     │
│  │  │ SUBMIT ├──► RETRYING   │ (max 3)                        │     │
│  │  └───┬────┘  └────────────┘                                │     │
│  │      │               │                                     │     │
│  │      ▼               ▼                                     │     │
│  │  ┌────────┐  ┌──────────┐                                  │     │
│  │  │ VERIFY │  │  FAILED  │                                  │     │
│  │  └───┬────┘  └──────────┘                                  │     │
│  │      ▼                                                     │     │
│  │  ┌───────────┐                                             │     │
│  │  │ COMPLETED │                                             │     │
│  │  └───────────┘                                             │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    │ value objects                                   │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ DetectedFormField (Value Object)                           │     │
│  │  - fieldName: String                    [form field name]  │     │
│  │  - fieldType: FieldType                [input type]        │     │
│  │  - selector: String                    [CSS/XPath]        │     │
│  │  - isRequired: boolean                 [required field]    │     │
│  │  - detectedLabel: String               [human label]      │     │
│  │  - mappedProfileField: String?          [profile field]    │     │
│  │                                                            │     │
│  │  FieldType: TEXT, EMAIL, PHONE, TEXTAREA, SELECT, FILE,    │     │
│  │             CHECKBOX, RADIO, DATE, HIDDEN                  │     │
│  └────────────────────────────────────────────────────────────┘     │
│                    │                                                 │
│                    ▼                                                 │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ AutomationEvidence (Value Object)                          │     │
│  │  - preSubmitScreenshot: String          [S3 URL]           │     │
│  │  - postSubmitScreenshot: String         [S3 URL]           │     │
│  │  - confirmationText: String             [page content]     │     │
│  │  - pageTitle: String                    [browser title]    │     │
│  │  - logs: List<AutomationLogEntry>      [action log]        │     │
│  │                                                            │     │
│  │  AutomationLogEntry(timestamp, level, action, details, ms) │     │
│  └────────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.4 C4 Level 4 — AI Provider Port (Interface Contract)

```
┌──────────────────────────────────────────────────────────────────────┐
│  AI PROVIDER PORT — Interface Contract                              │
│  Package: com.jobpilot.modules.ai.domain.port                       │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ public interface AIProviderPort {                          │     │
│  │                                                            │     │
│  │     // === Text Generation ===                              │     │
│  │     AiResponse generateText(AiRequest request);            │     │
│  │                                                            │     │
│  │     // === Streaming Generation ===                         │     │
│  │     Flux<AiChunk> generateStream(AiRequest request);       │     │
│  │                                                            │     │
│  │     // === Embeddings ===                                   │     │
│  │     List<Float> generateEmbedding(String text);            │     │
│  │                                                            │     │
│  │     // === Utility ===                                      │     │
│  │     int countTokens(String text);                           │     │
│  │ }                                                            │     │
│  └────────────────────────────────────────────────────────────┘     │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ public record AiRequest(                                  │     │
│  │     String model,                                          │     │
│  │     List<AiMessage> messages,                              │     │
│  │     double temperature,                                    │     │
│  │     int maxTokens,                                         │     │
│  │     List<String> stopSequences,                            │     │
│  │     ResponseFormat responseFormat,                         │     │
│  │     List<AiTool> tools                                     │     │
│  │ ) {}                                                        │     │
│  │                                                              │     │
│  │ public record AiMessage(                                   │     │
│  │     AiMessageRole role,    // SYSTEM, USER, ASSISTANT, TOOL │     │
│  │     String content,                                         │     │
│  │     String name                                             │     │
│  │ ) {}                                                        │     │
│  │                                                              │     │
│  │ public record AiResponse(                                  │     │
│  │     String content,                                         │     │
│  │     FinishReason finishReason,                              │     │
│  │     TokenUsage usage,                                       │     │
│  │     String modelUsed,                                       │     │
│  │     long latencyMs                                          │     │
│  │ ) {}                                                        │     │
│  │                                                              │     │
│  │ public record TokenUsage(                                  │     │
│  │     int promptTokens,                                       │     │
│  │     int completionTokens,                                   │     │
│  │     int totalTokens                                         │     │
│  │ ) {}                                                        │     │
│  │                                                              │     │
│  │ public enum AiMessageRole { SYSTEM, USER, ASSISTANT, TOOL } │     │
│  │ public enum FinishReason { STOP, LENGTH, CONTENT_FILTER,   │     │
│  │                            TOOL_CALLS, ERROR }             │     │
│  │ public enum ResponseFormat { JSON_OBJECT, TEXT }            │     │
│  │ public enum AiProviderType { OPENAI, ANTHROPIC,             │     │
│  │                              OLLAMA, GEMINI }              │     │
│  └────────────────────────────────────────────────────────────┘     │
│                                                                      │
│  Adapter Contract (all implementations must fulfill):                │
│  1. generateText: Must return within 30s or throw timeout            │
│  2. generateStream: Must emit completion signal or error            │
│  3. generateEmbedding: Must return fixed-dimension vector          │
│  4. countTokens: Must approximate within 10% accuracy              │
│  5. All implementations must be thread-safe                        │
│  6. Must not throw checked exceptions (wrap in AiException)        │
│  7. Must respect rate limit headers from provider                 │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.5 C4 Level 4 — Outbox Pattern Implementation

```
┌──────────────────────────────────────────────────────────────────────┐
│  TRANSACTIONAL OUTBOX PATTERN                                       │
│                                                                      │
│  Goal: Guarantee at-least-once event delivery without 2PC           │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ Write Path:                                                 │     │
│  │                                                              │     │
│  │ ApplicationService                                          │     │
│  │   │                                                          │     │
│  │   │ @Transactional                                          │     │
│  │   │ 1. Aggregate changes (DB writes)                        │     │
│  │   │ 2. domainEventBus.publish(event)  → writes to outbox   │     │
│  │   │ 3. Commit transaction                                   │     │
│  │   │    (aggregate data + outbox record in SAME transaction) │     │
│  │   │                                                          │     │
│  │   Outbox Table (PostgreSQL):                                 │     │
│  │     id: UUID (PK)                                            │     │
│  │     aggregate_type: String  (e.g. "Application")             │     │
│  │     aggregate_id: UUID                                       │     │
│  │     event_type: String (e.g. "ApplicationSubmittedEvent")    │     │
│  │     event_payload: JSONB   (serialized event)                │     │
│  │     trace_id: String       (distributed tracing)             │     │
│  │     status: String  (PENDING | PUBLISHED | FAILED)          │     │
│  │     retry_count: int                                         │     │
│  │     created_at: Instant                                      │     │
│  │     published_at: Instant?                                   │     │
│  │                                                              │     │
│  │   Index: (status, created_at) WHERE status = 'PENDING'      │     │
│  └────────────────────────────────────────────────────────────┘     │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐     │
│  │ Read / Publish Path:                                         │     │
│  │                                                              │     │
│  │ OutboxPoller (scheduled, runs every 1 second)                 │     │
│  │   │                                                          │     │
│  │   1. SELECT * FROM outbox WHERE status = 'PENDING'           │     │
│  │      ORDER BY created_at ASC LIMIT 100 FOR UPDATE SKIP LOCKED│     │
│  │                                                              │     │
│  │   2. For each record:                                        │     │
│  │      a. Deserialize event payload                            │     │
│  │      b. Publish to Kafka topic (event_type → topic mapping)  │     │
│  │      c. On success: UPDATE status = 'PUBLISHED'              │     │
│  │      d. On failure: increment retry_count,                   │     │
│  │         if retry_count > 10: status = 'FAILED', log alert    │     │
│  │                                                              │     │
│  │   3. If no records, sleep 1s                                 │     │
│  │                                                              │     │
│  │   Topic Mapping:                                             │     │
│  │     ApplicationSubmittedEvent  → "application.events"        │     │
│  │     UserRegisteredEvent        → "user.events"               │     │
│  │     AutomationCompletedEvent   → "automation.events"         │     │
│  │     InterviewSessionCompleted  → "interview.events"          │     │
│  └────────────────────────────────────────────────────────────┘     │
│                                                                      │
│  Topic Layout (Kafka):                                               │
│    - user.events          (3 partitions)                            │
│    - application.events   (3 partitions)                            │
│    - automation.events    (5 partitions)                            │
│    - interview.events     (2 partitions)                            │
│    - job.events           (3 partitions)                            │
│    - notification.events  (2 partitions)                            │
│    - billing.events       (2 partitions)                            │
│                                                                      │
│  Dead Letter Queue:                                                  │
│    - All topics have a corresponding .DLQ topic                    │
│    - After 10 retries, event goes to {topic}.DLQ                    │
│    - DLQ monitored by admin alerts                                   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Appendix A: C4 Notation Legend

```
[Person]        — A human user of the system
[Software Sys]  — The system being described
[Container]     — A deployable unit (process, DB, etc.)
[Component]     — A grouping of related code within a container
(->)            — Relationship with description

Color Coding:
  Blue   = Existing/person
  Green  = Software system
  Orange = Container
  Purple = Component
  Grey   = External/system
```

## Appendix B: Traceability Matrix

| C4 Element | HLD Reference | LLD Reference | SRS Reference |
|------------|---------------|---------------|---------------|
| Job Seeker (Person) | §1.3 | §3.1 | §4 |
| Admin (Person) | §1.3 | §18 | §4.2 |
| Core API Server | §1.3, §4.1 | §1.1, §3 | §14 |
| API Gateway | §14.3, §3.1 | §1.1 | §14.3 |
| PostgreSQL | §4.1, §7 | §2.1, §18.2 | §13.4 |
| Redis | §4.1, §23 | §25 | §13.4 |
| Kafka | §6.2, §4.1 | §6.2, Appx | §13.4 |
| Browser Automation Worker | §14, §16 | §16, §3.3 | §16 |
| AI Orchestration Worker | §15 | §6, §3.4 | §15 |
| Resume Aggregate | §12 | §5 | FR3 |
| Application Aggregate | §12 | §12 | FR5 |
| AI Provider Port | §15 | §6 | §15 |

---

*This C4 Architecture document provides four levels of abstraction — from the system context that non-technical stakeholders understand, down to the code-level interfaces that developers implement. Every component, container, and relationship is traced back to the HLD, LLD, and SRS.*

---

**End of C4 Architecture v1.0**
