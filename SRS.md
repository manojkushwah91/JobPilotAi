# JobPilot AI — Software Requirements Specification (SRS)

**Version:** 1.0  
**Status:** Draft  
**Product:** JobPilot AI — "The AI Career Operating System"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Executive Summary
2. Business Problem
3. Vision, Mission & Goals
4. Target Users & Personas
5. Functional Requirements
6. Non-Functional Requirements
7. Modules & Features
8. User Stories
9. Use Cases
10. Acceptance Criteria
11. Business Rules
12. System Constraints
13. Technology Stack
14. Architecture Overview
15. AI Architecture
16. Browser Automation Architecture
17. Security Architecture
18. Data Model (Conceptual)
19. API Design Philosophy
20. Frontend Architecture
21. Backend Architecture
22. Deployment Strategy
23. Scaling Strategy
24. Testing Strategy
25. Monitoring & Observability
26. Logging Strategy
27. Documentation Strategy
28. Development Roadmap
29. Future Scope
30. Risks & Mitigations
31. Success Metrics

---

## 1. Executive Summary

JobPilot AI is a next-generation SaaS platform that transforms the way professionals manage their career journeys. It acts as an "AI Career Operating System" — a single pane of glass for job search, application automation, interview preparation, skills development, and career planning. The platform combines large language model (LLM) AI, browser automation via Playwright Java, and enterprise-grade backend architecture (Java 21, Spring Boot, PostgreSQL, Redis) to deliver a seamless experience across web (Next.js) and mobile (React Native) surfaces.

The system is architected for scale from day one using Clean Architecture, Domain-Driven Design, CQRS, Event-Driven patterns, and a modular monolith that can decompose into microservices as the product grows.

---

## 2. Business Problem

### 2.1 The Problem Space

- **Fragmented job search:** Candidates juggle LinkedIn, Indeed, Glassdoor, company career portals, email threads, and spreadsheets. There is no single source of truth.
- **Repetitive manual work:** Each job application requires tailoring a resume, writing a cover letter, filling redundant form fields, and tracking follow-ups. This is hours per application.
- **Missed opportunities:** Job postings appear and disappear daily; candidates cannot monitor every source 24/7.
- **Poor interview preparation:** Most candidates prepare blindly without AI-powered insights into the company, role, and likely questions.
- **No career intelligence:** Professionals lack data-driven insights about their market value, skill gaps, optimal career moves, and industry trends.
- **Application burnout:** Sending 100+ applications with no structured tracking leads to frustration and lost follow-ups.

### 2.2 Market Gap

Existing tools address isolated pieces:
- **LinkedIn** — professional networking, limited job search
- **Indeed** — job aggregation, no AI or automation
- **Simplify.jobs** — autofill, no career intelligence
- **Resume.io** — resume builder, no automation
- **Huntr / Jopwell** — ATS lite, no AI

No platform combines *AI-powered job matching, automated application submission, intelligent ATS, interview prep, and career planning* into one unified system.

---

## 3. Vision, Mission & Goals

### 3.1 Vision

To become the operating system for every professional's career — the single platform where careers are managed, optimized, and accelerated.

### 3.2 Mission

Eliminate the friction of job searching and career management by combining artificial intelligence, browser automation, and elegant UX into a unified platform that gives professionals superhuman career capabilities.

### 3.3 Strategic Goals

| Goal | Description | Timeline |
|------|-------------|----------|
| G1 | Launch MVP with core job aggregation, AI resume builder, and application tracking | Q1 |
| G2 | Achieve 10,000 active users with < 2s p95 API latency | Q2 |
| G3 | Launch browser-automated application submission with 95%+ success rate | Q2 |
| G4 | Achieve SOC 2 Type I compliance readiness | Q3 |
| G5 | Support 500+ job boards with real-time aggregation | Q4 |
| G6 | Launch AI interview coach with 90% user satisfaction | Q4 |
| G7 | Support 100k+ concurrent users with 99.99% uptime | Year 2 |

---

## 4. Target Users & Personas

### 4.1 Primary Market

- **Tech professionals** (SWEs, DevOps, Data Scientists, PMs) — ages 22–45
- **Recent graduates** entering the workforce
- **Mid-career professionals** looking to pivot or advance
- **Freelancers / consultants** managing multiple opportunities

### 4.2 User Personas

#### Persona A: "Sarah" — Active Job Seeker (Power User)
- **Age:** 29
- **Role:** Senior Frontend Engineer (laid off)
- **Pain:** Sending 50+ applications/week manually, losing track of responses, overwhelmed
- **Needs:** Automated applications, intelligent ATS, AI interview prep
- **Tech Level:** High

#### Persona B: "Marcus" — Passive Candidate
- **Age:** 34
- **Role:** DevOps Engineer (employed, looking)
- **Pain:** Too busy to browse listings daily, wants alerts for perfect matches
- **Needs:** AI job matching, one-click applications, market salary insights
- **Tech Level:** Medium

#### Persona C: "Priya" — Recent Graduate
- **Age:** 22
- **Role:** CS Graduate (entry level)
- **Pain:** No interview experience, weak resume, no network
- **Needs:** Resume builder, interview simulator, skill gap analysis, networking templates
- **Tech Level:** Medium

#### Persona D: "Carlos" — Career Changer
- **Age:** 38
- **Role:** Project Manager → Product Manager (transitioning)
- **Pain:** No clarity on required skills, no roadmap for transition
- **Needs:** Career path planning, skills assessment, learning resource recommendations
- **Tech Level:** Low-Medium

#### Persona E: "Elena" — Recruiter / Hiring Manager (Secondary)
- **Age:** 31
- **Role:** Technical Recruiter
- **Pain:** Evaluating candidates is time-consuming
- **Needs:** Profile insights, skill verification reports (future)
- **Tech Level:** Medium

---

## 5. Functional Requirements

### FR1 — User Management
- FR1.1: Email/password registration with email verification
- FR1.2: OAuth 2.0 login (Google, LinkedIn, GitHub, Microsoft)
- FR1.3: Profile management (name, photo, location, work authorization, links)
- FR1.4: Role-based access control (FREE, PRO, ENTERPRISE tiers)
- FR1.5: Account deletion with GDPR-compliant data purge
- FR1.6: Session management (JWT access + refresh tokens)
- FR1.7: Multi-device session awareness

### FR2 — Job Aggregation & Search
- FR2.1: Real-time/near-real-time job listing aggregation from 20+ sources (LinkedIn, Indeed, Glassdoor, ZipRecruiter, Google Jobs, company career pages, etc.)
- FR2.2: Pluggable source adapters (Interface-based design for adding new sources)
- FR2.3: Full-text search with filtering (title, company, location, salary range, remote/onsite, experience level, date posted, industry)
- FR2.4: AI-powered semantic search ("find me a remote React job paying >$150k")
- FR2.5: Saved searches with push/email notifications
- FR2.6: De-duplication of identical listings across sources
- FR2.7: Job detail extraction (description, requirements, benefits, company info, salary)

### FR3 — Resume & Cover Letter Builder
- FR3.1: AI-powered resume creation from user profile + job description tailoring
- FR3.2: Multiple resume templates (ATS-optimized + modern designs)
- FR3.3: PDF, DOCX, and plain text export
- FR3.4: AI suggestions for bullet points, skills, and achievements
- FR3.5: Resume score (ATS compatibility rating)
- FR3.6: Cover letter generation with company/role-specific personalization
- FR3.7: Version history for resumes and cover letters
- FR3.8: Keyword gap analysis against target job descriptions

### FR4 — Automated Job Application (Browser Automation)
- FR4.1: Headless browser automation via Playwright Java
- FR4.2: Auto-fill application forms from user profile
- FR4.3: Upload tailored resume + cover letter per application
- FR4.4: Support for major ATS platforms (Greenhouse, Lever, Workday, Taleo, BambooHR, SuccessFactors)
- FR4.5: CAPTCHA detection and graceful fallback (notify user)
- FR4.6: Multi-step application orchestration with state machine
- FR4.7: Screenshot evidence of submitted applications
- FR4.8: Application success/failure reporting with error details
- FR4.9: Rate limiting and anti-detection measures
- FR4.10: Manual review queue for applications needing human intervention

### FR5 — Application Tracking System (ATS)
- FR5.1: Kanban pipeline view (Saved → Applied → Phone Screen → Technical → On-site → Offer → Rejected → Accepted)
- FR5.2: Manual and automatic status updates
- FR5.3: Notes and attachments per application
- FR5.4: Follow-up reminders and scheduling
- FR5.5: Company research integration (LinkedIn, Crunchbase, Glassdoor)
- FR5.6: Salary and compensation tracking per application
- FR5.7: Application timeline/history view
- FR5.8: Bulk operations (archive, status change)
- FR5.9: Email integration (sync application-related emails)

### FR6 — Interview Preparation
- FR6.1: AI-powered interview question prediction based on job description + company
- FR6.2: Mock interview simulator (text + voice modes)
- FR6.3: Behavioral question bank (STAR method training)
- FR6.4: Technical question bank (coding, system design, domain-specific)
- FR6.5: Answer scoring and feedback via AI
- FR6.6: Company-specific interview insights (scraped from Glassdoor/Blind)
- FR6.7: Practice session recording and transcription
- FR6.8: Progress tracking across practice sessions

### FR7 — Career Path Planning
- FR7.1: AI career path recommendations based on current role → target role
- FR7.2: Skills gap analysis (current skills vs target role requirements)
- FR7.3: Learning resource recommendations (courses, books, certifications)
- FR7.4: Salary benchmarking by role, location, experience, industry
- FR7.5: Market demand trends visualization
- FR7.6: Goal setting and milestone tracking

### FR8 — Networking & Outreach
- FR8.1: AI-generated connection request messages (LinkedIn, email)
- FR8.2: Follow-up message templates
- FR8.3: Outreach campaign tracking
- FR8.4: Referral request automation
- FR8.5: Networking CRM (who you know, where they work, last interaction)

### FR9 — Analytics & Dashboard
- FR9.1: Application funnel metrics (applied → interview → offer conversion)
- FR9.2: Response rate analytics by job board, industry, role type
- FR9.3: Resume performance score vs. industry benchmarks
- FR9.4: Weekly/monthly activity reports
- FR9.5: Time-to-offer tracking
- FR9.6: Salary negotiation insights

### FR10 — Notification System
- FR10.1: Email notifications (application status, new matches, messages)
- FR10.2: Push notifications (web + mobile)
- FR10.3: In-app notification center
- FR10.4: Digest preferences (real-time, daily, weekly)
- FR10.5: Notification templates with personalization

### FR11 — Payment & Subscription
- FR11.1: Tiered subscription plans (Free / Pro / Enterprise)
- FR11.2: Stripe integration for payment processing
- FR11.3: Free tier with limited applications/month
- FR11.4: Pro tier with unlimited applications, AI features, automation
- FR11.5: Enterprise tier with team management, custom integrations
- FR11.6: Usage metering and billing alerts
- FR11.7: Invoice generation and payment history

### FR12 — Admin Panel
- FR12.1: User management (view, suspend, delete)
- FR12.2: Subscription management (override, refund, cancel)
- FR12.3: Job source management (add/remove/configure adapters)
- FR12.4: System health dashboard
- FR12.5: Content moderation (reported issues)
- FR12.6: Feature flag management
- FR12.7: Analytics overview

---

## 6. Non-Functional Requirements

### NFR1 — Performance
- NFR1.1: API p95 response time < 500ms for read operations
- NFR1.2: API p95 response time < 2s for write operations (AI-heavy endpoints allowed 10s with streaming)
- NFR1.3: Page load time < 2s (LCP) on modern browsers
- NFR1.4: Job search index query time < 200ms
- NFR1.5: Concurrent user support scaling to 100k (Year 2 target)
- NFR1.6: Browser automation job throughput: 10 concurrent sessions per node

### NFR2 — Availability
- NFR2.1: 99.9% uptime SLA (MVP), 99.99% (mature)
- NFR2.2: Planned downtime window: Sundays 2–4 AM UTC
- NFR2.3: Graceful degradation under load (circuit breakers, fallbacks)

### NFR3 — Security
- NFR3.1: All data encrypted at rest (AES-256) and in transit (TLS 1.3)
- NFR3.2: JWT tokens with 15-min access + 7-day refresh rotation
- NFR3.3: OWASP Top 10 protection (XSS, CSRF, SQLi, SSRF, etc.)
- NFR3.4: Rate limiting per user/IP (100 req/min general, 10 req/min for AI)
- NFR3.5: GDPR & CCPA compliance (data export, deletion, consent)
- NFR3.6: SOC 2 Type I readiness
- NFR3.7: Secrets management via HashiCorp Vault / AWS Secrets Manager
- NFR3.8: RBAC enforcement at API gateway + service level
- NFR3.9: Audit logging for all sensitive operations
- NFR3.10: No plaintext secrets in code or config

### NFR4 — Scalability
- NFR4.1: Horizontal scaling for stateless services
- NFR4.2: Database read replicas for query-heavy workloads
- NFR4.3: Redis caching for session data, job listings, AI responses
- NFR4.4: Event-driven async processing for browser automation
- NFR4.5: Database partitioning/sharding ready (by tenant/user_id)

### NFR5 — Reliability
- NFR5.1: Automated failover for critical services
- NFR5.2: Retry with exponential backoff for 3rd-party API calls
- NFR5.3: Idempotency keys for payment and application operations
- NFR5.4: Bulkhead pattern for AI service isolation
- NFR5.5: Saga pattern for distributed transactions

### NFR6 — Maintainability
- NFR6.1: Clean Architecture with strict layer dependencies
- NFR6.2: Modular monolith with clear bounded contexts
- NFR6.3: Minimum 80% unit test coverage (core domain)
- NFR6.4: Integration tests for all external adapters
- NFR6.5: API documentation via OpenAPI 3.1
- NFR6.6: Feature flags for gradual rollout

### NFR7 — Usability
- NFR7.1: WCAG 2.1 AA accessibility compliance
- NFR7.2: Responsive design (mobile-first, all breakpoints)
- NFR7.3: Maximum 3 clicks to reach any feature
- NFR7.4: Onboarding wizard for new users
- NFR7.5: Keyboard navigable
- NFR7.6: Dark mode support

### NFR8 — Portability
- NFR8.1: Docker containerization for all services
- NFR8.2: Kubernetes manifests for orchestration
- NFR8.3: Cloud-agnostic core (AWS primary, GCP/Azure ready)
- NFR8.4: Database migration automated via Flyway

---

## 7. Modules & Features

### M1 — Core Platform Module
- User registration & authentication
- Subscription & billing management
- Profile management
- Notification orchestration
- Admin console

### M2 — Job Intelligence Module
- Job source adapters (pluggable)
- Job listing aggregation & deduplication
- Full-text + semantic search
- Job alert engine
- Company intelligence (scraped data enrichment)

### M3 — Resume & Documents Module
- Resume builder engine
- Cover letter generator
- Template management
- ATS scoring engine
- Document version control
- Export service (PDF, DOCX, TXT)

### M4 — Browser Automation Module
- Playwright Java orchestration engine
- Application form auto-fill engine
- ATS-specific adapters
- CAPTCHA detection service
- Session/proxy management
- Application state machine
- Screenshot & evidence service

### M5 — ATS Module
- Kanban pipeline engine
- Status transition management
- Notes & attachments service
- Follow-up scheduler
- Email integration (IMAP/SMTP sync)
- Salary tracking

### M6 — Interview Coach Module
- Question prediction engine (AI)
- Mock interview session manager
- Voice transcription service
- Answer scoring engine
- Progress tracking
- Question bank management

### M7 — Career Intelligence Module
- Career path suggestion engine
- Skills gap analyzer
- Salary benchmarking service
- Market trend analyzer
- Learning resource aggregator

### M8 — Networking Module
- Message generator (AI)
- Outreach campaign manager
- CRM for contacts
- Referral tracking

### M9 — Analytics Module
- Metrics calculation engine
- Report generator
- Dashboard data aggregator
- Export service (CSV, PDF)

### M10 — AI Orchestration Module
- LLM provider abstraction (OpenAI, Anthropic, open-source fallback)
- Prompt management & versioning
- Response caching layer
- Token usage tracking & cost management
- Embedding service for semantic search
- Fine-tuning pipeline (future)

---

## 8. User Stories

| ID | Story |
|----|-------|
| US-001 | As a user, I want to register with Google or LinkedIn so that I can start quickly without creating another password. |
| US-002 | As a user, I want to see a unified dashboard of all my job applications so that I know where I stand. |
| US-003 | As a user, I want to search jobs with natural language ("remote senior Java jobs >$150k") so that I find relevant listings faster. |
| US-004 | As a user, I want the AI to tailor my resume to each job description so that I pass ATS filters more often. |
| US-005 | As a user, I want the system to auto-apply to jobs matching my criteria so that I save hours of manual form filling. |
| US-006 | As a user, I want to receive daily alerts about new matching jobs so that I never miss an opportunity. |
| US-007 | As a user, I want to practice mock interviews with AI so that I can improve before the real thing. |
| US-008 | As a user, I want AI-generated cover letters so that I don't have to write them from scratch. |
| US-009 | As a user, I want to know my resume's ATS score so that I can improve it before applying. |
| US-010 | As a user, I want to see my application conversion funnel so that I can optimize my strategy. |
| US-011 | As a user, I want the system to suggest career paths based on my profile so that I can plan my growth. |
| US-012 | As a user, I want to know what skills I'm missing for my target role so that I can learn them. |
| US-013 | As a user, I want to generate personalized LinkedIn connection messages so that my outreach is more effective. |
| US-014 | As a user, I want to set up follow-up reminders so that I never ghost a recruiter. |
| US-015 | As a user, I want to see salary benchmarks for my role and location so that I negotiate better. |
| US-016 | As an admin, I want to view system health metrics so that I can proactively address issues. |
| US-017 | As an admin, I want to manage user subscriptions so that I can handle edge cases. |

---

## 9. Use Cases

### UC-1: User Registration
**Actors:** Unregistered User  
**Flow:**
1. User navigates to /register
2. Chooses email/password or OAuth provider
3. System validates input
4. If email: sends verification email
5. If OAuth: creates account with provider data
6. System creates default profile, free-tier subscription
7. Onboarding wizard is triggered
8. User is redirected to dashboard

### UC-2: Automated Job Application
**Actors:** Authenticated Pro/Enterprise User  
**Flow:**
1. User configures job search criteria (title, location, remote, salary)
2. User uploads base resume and cover letter templates
3. AI scans job description and tailors resume + cover letter
4. System queues application for automation
5. Playwright engine opens headless browser
6. Engine navigates to application URL
7. Engine identifies form fields via DOM analysis
8. Engine fills fields from user profile + tailored documents
9. Engine submits application
10. Engine captures screenshot as evidence
11. System updates ATS pipeline with status "Applied"
12. Email notification sent to user

**Alternative Flow (CAPTCHA):**
- 8a. Engine detects CAPTCHA
- 8b. Engine marks application as "Requires Manual Action"
- 8c. User receives notification to complete CAPTCHA manually

### UC-3: AI Interview Practice
**Actors:** Authenticated User  
**Flow:**
1. User selects target role/company
2. System predicts likely questions based on role + company research
3. User selects practice mode (text/voice)
4. AI presents first question
5. User responds
6. AI analyzes response for content, clarity, structure
7. AI provides score and improvement suggestions
8. Proceeds to next question or ends session
9. Session summary is saved to profile

### UC-4: Career Path Analysis
**Actors:** Authenticated User  
**Flow:**
1. User inputs current role and target role
2. System analyzes skill requirements for both roles
3. System identifies gap skills
4. System recommends learning resources for each gap
5. System estimates timeline for transition
6. System shows salary progression data
7. User can save career plan and track progress

---

## 10. Acceptance Criteria

### AC-1: User Registration
```
GIVEN a user on the registration page
WHEN they submit valid registration details
THEN an account is created
AND a verification email is sent
AND the user is redirected to onboarding
AND the free tier subscription is activated

GIVEN a user registering with an existing email
WHEN they submit the form
THEN an error "Email already registered" is shown
AND no duplicate account is created
```

### AC-2: Automated Application
```
GIVEN a Pro user with a valid job application URL
WHEN the automation engine processes the application
THEN the form is filled with correct user data
AND the tailored resume is attached
AND the tailored cover letter is attached
AND the application is successfully submitted
AND a screenshot is captured
AND the ATS pipeline is updated
AND a notification is sent

GIVEN the automation encounters a CAPTCHA
WHEN the engine detects it
THEN the application is moved to "Requires Manual Action"
AND the user is notified with a direct link
```

### AC-3: Resume ATS Score
```
GIVEN a user uploads a resume for a specific job description
WHEN the AI scoring engine evaluates it
THEN a score from 0-100 is returned
AND specific improvement suggestions are provided
AND missing keywords from the job description are listed
```

---

## 11. Business Rules

| BR-ID | Rule |
|-------|------|
| BR-1 | Free tier users can apply to max 10 jobs/month via automation |
| BR-2 | Pro tier users have unlimited automated applications |
| BR-3 | AI resume tailoring is available only for Pro+ tiers |
| BR-4 | Interview practice sessions: Free = 5/month, Pro = unlimited |
| BR-5 | Career path analysis is available for all tiers (Pro gets deeper insights) |
| BR-6 | Job alerts: Free = 3 saved searches, Pro = unlimited |
| BR-7 | ATS pipeline entries are auto-deleted after 18 months of inactivity |
| BR-8 | Browser automation sessions are limited to 10 concurrent per user |
| BR-9 | Rate limit for job board scraping: max 100 pages/hour per source |
| BR-10 | User data export must be available within 72 hours of request |
| BR-11 | Account deletion triggers cascade: anonymize analytics, delete PII, retain anonymized usage stats |
| BR-12 | Password must be 12+ chars with 1 upper, 1 lower, 1 digit, 1 special |

---

## 12. System Constraints

| C-ID | Constraint | Rationale |
|------|------------|-----------|
| C-1 | Max API payload: 10MB | Prevent abuse, ensure network performance |
| C-2 | Max concurrent automation sessions per node: 10 | Browser automation is resource-intensive |
| C-3 | AI response max tokens: 4096 | Cost management and latency control |
| C-4 | Job search index: max 100k results per query | Relevance degrades beyond this |
| C-5 | Resume file max: 10MB | Storage and processing limits |
| C-6 | Max saved job searches: 50 per user | Database query optimization |
| C-7 | Email sending rate: max 1000/hour (per service) | SMTP provider limits |
| C-8 | WebSocket connection max: 10k per node | Memory constraints |
| C-9 | Database connection pool: max 200 per service instance | PostgreSQL limits |
| C-10 | File upload types: PDF, DOCX, TXT, PNG, JPG | Security and processing |

---

## 13. Technology Stack

### 13.1 Backend Core
| Technology | Purpose | Justification |
|------------|---------|---------------|
| Java 21 (LTS) | Primary language | Virtual threads, pattern matching, sealed classes, records — modern Java |
| Spring Boot 3.x | Application framework | Mature, production-proven, excellent ecosystem |
| Spring Security 6.x | AuthN/AuthZ | OAuth 2.0 resource server, JWT, method security |
| Spring Data JPA / Hibernate | ORM | Mature, widely understood, cache integration |
| Spring Data Redis | Caching | Distributed cache, rate limiting, session store |
| Spring Cloud Gateway | API Gateway | Reactive, filter chain, rate limiting |
| Spring for Apache Kafka | Event bus | Durable, replayable, schema registry |
| Flyway | Database migration | Version-controlled, repeatable, safe |
| MapStruct | DTO mapping | Compile-time, type-safe, zero reflection |
| OpenAPI 3.1 / SpringDoc | API docs | Auto-generated, interactive docs |
| Lombok | Boilerplate reduction | Records cover most, Lombok for legacy patterns |

### 13.2 AI & Machine Learning
| Technology | Purpose | Justification |
|------------|---------|---------------|
| Spring AI | AI integration abstraction | Consistent API across LLM providers |
| OpenAI GPT-4 / Claude 3 | Primary LLM | State-of-the-art for text generation |
| LangChain4j (optional) | Advanced AI chains | RAG, agent patterns if needed |
| pgvector | Embedding storage | Native PostgreSQL, no extra infra |
| Sentence-Transformers (SBERT) | Local embeddings | Fallback, no API cost |

### 13.3 Browser Automation
| Technology | Purpose | Justification |
|------------|---------|---------------|
| Playwright Java | Browser automation | Modern, reliable, cross-browser, multi-tab |
| Chrome/Chromium (headless) | Browser engine | Market standard, DevTools Protocol |
| Proxy rotator (custom) | Anti-detection | Avoid IP blocking during automated applications |

### 13.4 Database & Storage
| Technology | Purpose | Justification |
|------------|---------|---------------|
| PostgreSQL 16 | Primary database | Mature, JSONB, full-text search, pgvector, CTEs |
| pgvector | Vector embeddings | Store and search job/resume embeddings |
| Redis 7 | Cache / rate-limit / session | Blazing fast, battle-tested in top tech companies |
| Amazon S3 / MinIO | File storage | Resume PDFs, cover letters, screenshots, static assets |

### 13.5 Frontend
| Technology | Purpose | Justification |
|------------|---------|---------------|
| Next.js 14 (App Router) | Web framework | SSR, ISR, streaming, React Server Components |
| TypeScript 5 | Language | Type safety, developer experience |
| Tailwind CSS 4 | Styling | Utility-first, consistent design system |
| shadcn/ui | Component library | Accessible, customizable, tree-shakeable |
| React Query (TanStack Query) | Data fetching | Caching, deduplication, optimistic updates |
| Zustand | State management | Lightweight, TypeScript-first, no boilerplate |
| React Hook Form + Zod | Form management | Type-safe validation, performant |
| Framer Motion | Animations | Declarative, performant, accessible |
| React Flow | Pipeline visualization (optional) | For career path visualizations |

### 13.6 Infrastructure
| Technology | Purpose | Justification |
|------------|---------|---------------|
| Docker | Containerization | Consistent environments, local dev parity |
| Kubernetes (K3s / EKS) | Orchestration | Auto-scaling, self-healing, rolling updates |
| Terraform (OpenTofu) | IaC (Infrastructure as Code) | Declarative, stateful, cloud-agnostic |
| GitHub Actions | CI/CD | Tight integration, self-hosted runners for Playwright |
| ArgoCD | GitOps | Declarative deployment, drift detection |
| Nginx / Traefik | Reverse proxy / ingress | L7 routing, SSL termination, rate limiting |
| Prometheus + Grafana | Monitoring | Industry standard, rich dashboard ecosystem |
| ELK / Loki + Grafana | Logging | Structured logging, aggregation, search |
| OpenTelemetry | Distributed tracing | Vendor-neutral, context propagation |
| Sentry | Error tracking | Real-time error monitoring, performance traces |

### 13.7 Testing
| Technology | Purpose | Justification |
|------------|---------|---------------|
| JUnit 5 + AssertJ | Unit testing | Standard for Java |
| Testcontainers | Integration testing | Throwaway DB, Redis, Kafka containers |
| Mockito | Mocking | Industry standard |
| WireMock | HTTP stub/mock | External API testing |
| Playwright (Test) | E2E testing | Same lib as automation, consistent DX |
| k6 (Grafana) | Load testing | Scriptable, metrics-rich, CI-friendly |
| ArchUnit | Architecture testing | Enforce Clean Architecture layers |
| Jacoco | Coverage | Industry standard, CI integration |
| OWASP ZAP | Security testing | DAST scanning in CI pipeline |

---

## 14. Architecture Overview

### 14.1 Architectural Style: Modular Monolith with Event-Driven Microservices Readiness

```
Phase 1 (MVP): Modular Monolith
- Single deployable JAR
- Clear bounded contexts (packages/ modules)
- Strict layer separation (Clean Architecture)
- In-process communication via domain events
- Can be deployed as multiple instances behind load balancer

Phase 2 (Scale): Extracted Microservices
- Extract Browser Automation Module as separate service (resource-heavy)
- Extract AI Orchestration as separate service
- Extract Job Aggregation as separate service (I/O heavy)
- Communication via Kafka events + gRPC for synchronous needs
```

### 14.2 Clean Architecture Layers (Per Module)

```
┌──────────────────────────────────────────────┐
│                  Presentation                  │
│   REST Controllers / WebSocket / GraphQL     │
├──────────────────────────────────────────────┤
│                  Application                   │
│   Use Cases / DTOs / Ports (Inbound)        │
├──────────────────────────────────────────────┤
│                    Domain                      │
│   Entities / Value Objects / Aggregates      │
│   Domain Events / Domain Services            │
├──────────────────────────────────────────────┤
│                 Infrastructure                 │
│   Persistence / Messaging / AI Providers    │
│   Browser Automation / External APIs         │
│   Ports (Outbound) Implementations          │
└──────────────────────────────────────────────┘
```

### 14.3 High-Level System Context

```
                    ┌─────────────┐
                    │   Clients    │
                    │ (Web/Mobile) │
                    └──────┬──────┘
                           │ HTTPS/WSS
                    ┌──────▼──────┐
                    │   CDN/S3     │
                    │ (Static     │
                    │  assets)    │
                    └─────────────┘
                           │
                    ┌──────▼──────┐
                    │  Cloudflare  │
                    │  (WAF, DDOS) │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  API Gateway │
                    │ (Spring     │
                    │  Cloud GW)  │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼────┐ ┌────▼────┐ ┌────▼────┐
       │  Auth     │ │ Core    │ │  AI     │
       │  Service  │ │ Service │ │ Service │
       └──────┬────┘ └────┬────┘ └────┬────┘
              │            │            │
       ┌──────▼────┐ ┌────▼────┐ ┌────▼────┐
       │ Postgres  │ │ Redis   │ │ Kafka   │
       │ (Primary) │ │ (Cache) │ │ (Events)│
       └───────────┘ └─────────┘ └─────────┘
              │
       ┌──────▼──────────────┐
       │ Browser Automation  │
       │ Service (Playwright)│
       └─────────────────────┘
              │
       ┌──────▼──────────────┐
       │ External Job Boards  │
       │ (LinkedIn, Indeed...)│
       └─────────────────────┘
```

### 14.4 Key Architectural Decisions

| ADR-ID | Decision | Rationale |
|--------|----------|-----------|
| ADR-1 | Modular monolith over microservices for MVP | Faster iteration, simpler deployment, avoids distributed transaction complexity until needed |
| ADR-2 | Spring Cloud Gateway over Nginx-only | Native Spring integration, dynamic routing, rate limiting, filter chains |
| ADR-3 | Kafka over RabbitMQ | Durability, replay, partitioning for scale-out, schema evolution via Schema Registry |
| ADR-4 | PostgreSQL over CockroachDB or Aurora | Sufficient scaling with read replicas + connection pooling; pgvector for AI embeddings |
| ADR-5 | JWT + OAuth2 over Session-based auth | Stateless, scalable, mobile/web compatible, industry standard |
| ADR-6 | Playwright Java over Selenium | Modern API, faster, better debugging, Microsoft-backed, supports Chrome DevTools Protocol |
| ADR-7 | Clean Architecture over simple MVC | Future-proof, testable, framework-agnostic core domain |
| ADR-8 | CQRS for ATS and Analytics | Different read/write patterns; analytics queries don't affect transactional throughput |

---

## 15. AI Architecture

### 15.1 AI Provider Abstraction

```
┌──────────────────────────────────────┐
│         AIOrchestratorService        │
│  (Facade — Core Application Layer)  │
├──────────────────────────────────────┤
│              Port (Interface)         │
│            LLMProviderPort           │
├──────────────────────────────────────┤
│          Adapter Implementations      │
│  ┌──────────┐ ┌──────────┐ ┌───────┐ │
│  │ OpenAI   │ │ Anthropic│ │  Local│ │
│  │ Adapter  │ │ Adapter  │ │ HF    │ │
│  └──────────┘ └──────────┘ └───────┘ │
└──────────────────────────────────────┘
```

### 15.2 AI Capabilities Mapping

| Feature | AI Model | Strategy |
|---------|----------|----------|
| Resume tailoring | GPT-4 / Claude 3 | Few-shot prompting with user profile + job description |
| Cover letter generation | GPT-4 / Claude 3 | Template + personalization prompt |
| ATS scoring | GPT-4 / Claude 3 + Heuristic | Keyword matching + semantic analysis |
| Interview question prediction | GPT-4 / Claude 3 | Role + company + industry analysis |
| Answer scoring | GPT-4 / Claude 3 | Rubric-based evaluation |
| Career path suggestion | GPT-4 / Claude 3 + Knowledge graph | Vector similarity + curated rules |
| Skills gap analysis | Embedding model + GPT-4 | Cosine similarity on skill embeddings |
| Semantic job search | Embedding model (pgvector) | Cosine similarity on job/resume embeddings |
| Networking message gen | GPT-4 / Claude 3 | Context-aware personalization |
| Salary insights | Data-driven (aggregated) + LLM augmentation | No hallucination on numbers — data first |

### 15.3 Prompt Management

- All prompts stored in version-controlled files (not hardcoded)
- Prompt templates with variables: `{user_profile}`, `{job_description}`, `{company_info}`
- Prompt versioning tied to application version
- A/B testing framework for prompt effectiveness

### 15.4 Cost Optimization

- Semantic search uses local embedding models (no API cost per query)
- LLM calls cached in Redis with TTL based on use case
- Response streaming to improve perceived latency
- Token budgeting per user/tier
- Fallback to smaller model (GPT-3.5 / Claude Haiku) for routine tasks

---

## 16. Browser Automation Architecture

### 16.1 Design Principles

- **Stateless Orchestration:** Each automation session is isolated and ephemeral
- **Resilience by Default:** Retry with backoff, graceful degradation, circuit breaker
- **Observability:** Every action is logged with screenshots and timing
- **Anti-Detection:** Realistic user agent, viewport, mouse movements, typing speed variation
- **Rate Limited:** Polite crawling — respect robots.txt, limit concurrent sessions

### 16.2 Architecture

```
┌────────────────────────────────────────────┐
│         AutomationOrchestrator              │
│  (Manages sessions, queues, state machine) │
├────────────────────────────────────────────┤
│                  ▼                           │
│    ┌──────────────────────────────┐        │
│    │   FormDetectionEngine        │        │
│    │   (Analyzes DOM, identifies  │        │
│    │    fields, maps to profile)  │        │
│    └──────────────────────────────┘        │
│                  ▼                           │
│    ┌──────────────────────────────┐        │
│    │   FormFillEngine             │        │
│    │   (Types text, uploads files,│        │
│    │    selects dropdowns)       │        │
│    └──────────────────────────────┘        │
│                  ▼                           │
│    ┌──────────────────────────────┐        │
│    │   SubmissionEngine           │        │
│    │   (Clicks submit, captures   │        │
│    │    confirmation screenshot) │        │
│    └──────────────────────────────┘        │
├────────────────────────────────────────────┤
│              State Machine                  │
│  INIT → FORM_DETECT → FORM_FILL →          │
│  SUBMIT → VERIFY → COMPLETE                │
│         ↘ CAPTCHA → MANUAL_REQUIRED        │
│         ↘ ERROR → RETRY → FAILED           │
└────────────────────────────────────────────┘
```

### 16.3 ATS Platform Adapters

Each major ATS platform requires a dedicated adapter:
- **GreenhouseAdapter:** Known form structure, field IDs
- **LeverAdapter:** Different DOM structure, custom fields
- **WorkdayAdapter:** Complex multi-page forms, JavaScript-heavy
- **TaleoAdapter:** Legacy, table-based layout, slower
- **BambooHRAdapter:** Simple, predictable
- **SuccessFactorsAdapter:** SAP-based, complex

Strategy: Generic DOM analyzer first, then ATS-specific optimizations.

### 16.4 Anti-Detection Measures

- Random viewport sizes (1280–1920px wide)
- Human-like typing speed (50–150ms between keystrokes)
- Random mouse movements (Playwright mouse.move with bezier curves)
- User agent rotation
- Proxy rotation (residential proxies for sensitive boards)
- Session timing randomization (not always same speed)
- Cookie persistence for known sites

### 16.5 Infrastructure for Automation

- Dedicated browser automation service (separate JVM process)
- Each session runs in its own browser context
- Sessions pooled and queued via Kafka
- Headless Chrome in Docker containers
- Resource limits: 2 CPU cores, 4GB RAM per automation node

---

## 17. Security Architecture

### 17.1 Authentication Flow

```
┌──────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│Client│     │  Gateway  │     │  Auth    │     │ External │
│      │     │          │     │  Service │     │ OAuth    │
└──┬───┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
   │ POST /login  │                │                 │
   │─────────────►│                │                 │
   │              │ POST /auth/login                │
   │              │───────────────►│                 │
   │              │                │ If OAuth:      │
   │              │                │────────────────►│
   │              │                │◄────────────────│
   │              │  tokens        │                 │
   │              │◄───────────────│                 │
   │ 200 + JWT    │                │                 │
   │◄─────────────│                │                 │
```

### 17.2 Token Strategy

| Token | Type | Lifetime | Storage | Purpose |
|-------|------|----------|---------|---------|
| Access Token | JWT (signed RS256) | 15 minutes | Memory (httpOnly cookie) | API authorization |
| Refresh Token | Opaque (UUID) | 7 days | Redis + httpOnly cookie | Obtain new access token |
| Reset Token | Opaque (UUID) | 1 hour | Redis hashed | Password reset |

### 17.3 Authorization

- Role-Based Access Control (RBAC) — roles: `FREE_USER`, `PRO_USER`, `ENTERPRISE_USER`, `ADMIN`
- Permission-based checks via Spring Security method annotations: `@PreAuthorize("hasRole('PRO_USER')")`
- Resource-level authorization: user can only access own data
- API Gateway validates JWT, passes claims downstream via headers

### 17.4 Data Protection

- **Encryption at rest:** AES-256 for PII columns via PostgreSQL `pgcrypto`
- **Encryption in transit:** TLS 1.3, HSTS headers
- **Secrets:** HashiCorp Vault or AWS Secrets Manager — NEVER in config files
- **Database:** Connection encrypted, field-level encryption for sensitive PII
- **Backup:** Encrypted backups, 30-day retention, point-in-time recovery

### 17.5 OWASP Protections

| Threat | Mitigation |
|--------|------------|
| XSS | Content Security Policy, React's built-in escaping, httpOnly cookies |
| CSRF | SameSite=Strict cookies, CSRF tokens for state-changing endpoints |
| SQL Injection | Parameterized queries (JPA/Hibernate), never concatenation |
| SSRF | URL allowlist for outbound requests, no user-controlled redirects |
| Rate Limiting | 100 req/min general, 10 req/min AI, per-user + per-IP tracking |
| Brute Force | Account lockout after 5 failed attempts (15-min cooldown) |
| Broken Authentication | OWASP ASVS compliance, no custom crypto |
| Insecure Direct Object Ref | UUIDs instead of sequential IDs, ownership verification |

### 17.6 Compliance

- **GDPR:** Data export API, account deletion with cascade, consent records, privacy policy
- **CCPA:** Opt-out mechanism, data inventory
- **SOC 2:** Audit logging (all sensitive operations), access reviews, change management

---

## 18. Data Model (Conceptual)

### 18.1 Core Entities

```
User
└── id: UUID (PK)
└── email: String (unique, indexed)
└── password_hash: String
└── oauth_provider: Enum [GOOGLE, LINKEDIN, GITHUB, MICROSOFT]
└── oauth_id: String
└── role: Enum [FREE, PRO, ENTERPRISE, ADMIN]
└── profile: UserProfile (1:1)
└── created_at: Timestamp
└── updated_at: Timestamp
└── deleted_at: Timestamp (soft delete)
└── email_verified_at: Timestamp

UserProfile
└── id: UUID (PK)
└── user_id: UUID (FK)
└── full_name: String
└── headline: String
└── phone: String (encrypted)
└── location: String
└── work_authorization: JSONB
└── linkedin_url: String
└── github_url: String
└── portfolio_url: String
└── skills: String[] (indexed)
└── experience: Experience[] (JSONB or related table)
└── education: Education[] (JSONB or related table)
└── preferences: JSONB

JobListing
└── id: UUID (PK)
└── source: Enum [LINKEDIN, INDEED, GLASSDOOR, COMPANY, MANUAL]
└── source_id: String (source's internal ID, for dedup)
└── title: String (indexed)
└── company: String (indexed)
└── company_logo_url: String
└── location: String
└── remote_type: Enum [ONSITE, REMOTE, HYBRID]
└── salary_min: Decimal
└── salary_max: Decimal
└── salary_currency: String
└── description: Text
└── requirements: JSONB
└── benefits: JSONB
└── application_url: String
└── posted_at: Timestamp
└── scraped_at: Timestamp
└── is_active: Boolean
└── embeddings: Vector(1536) (pgvector)
└── created_at: Timestamp

Resume
└── id: UUID (PK)
└── user_id: UUID (FK)
└── title: String
└── template_id: String
└── content: JSONB (structured resume data)
└── ats_score: Integer
└── version: Integer
└── file_url: String (S3)
└── created_at: Timestamp
└── updated_at: Timestamp

Application
└── id: UUID (PK)
└── user_id: UUID (FK)
└── job_listing_id: UUID (FK)
└── resume_id: UUID (FK)
└── cover_letter_id: UUID (FK)
└── status: Enum [SAVED, APPLIED, PHONE_SCREEN, TECHNICAL, ONSITE, OFFER, ACCEPTED, REJECTED, WITHDRAWN]
└── automation_status: Enum [PENDING, IN_PROGRESS, SUBMITTED, CAPTCHA, FAILED, MANUAL]
└── application_data: JSONB (form fields submitted)
└── evidence_screenshot_url: String
└── notes: Text
└── salary_offered: Decimal
└── applied_at: Timestamp
└── updated_at: Timestamp
└── created_at: Timestamp

InterviewSession
└── id: UUID (PK)
└── user_id: UUID (FK)
└── target_role: String
└── target_company: String
└── mode: Enum [TEXT, VOICE]
└── questions: Question[] (JSONB)
└── responses: Response[] (JSONB)
└── overall_score: Decimal
└── feedback: JSONB
└── duration_seconds: Integer
└── created_at: Timestamp

CareerPath
└── id: UUID (PK)
└── user_id: UUID (FK)
└── current_role: String
└── target_role: String
└── gaps: JSONB (skills gap analysis)
└── recommendations: JSONB (resources, courses)
└── timeline_estimate: String
└── created_at: Timestamp

Subscription
└── id: UUID (PK)
└── user_id: UUID (FK)
└── plan: Enum [FREE, PRO, ENTERPRISE]
└── stripe_customer_id: String
└── stripe_subscription_id: String
└── status: Enum [ACTIVE, CANCELED, PAST_DUE, EXPIRED]
└── current_period_start: Timestamp
└── current_period_end: Timestamp
└── canceled_at: Timestamp
└── created_at: Timestamp

Notification
└── id: UUID (PK)
└── user_id: UUID (FK)
└── type: Enum [APPLICATION_STATUS, JOB_ALERT, FOLLOW_UP, SYSTEM]
└── title: String
└── body: Text
└── data: JSONB (actionable payload)
└── read_at: Timestamp
└── created_at: Timestamp

AuditLog
└── id: UUID (PK)
└── user_id: UUID (FK)
└── action: String
└── resource_type: String
└── resource_id: String
└── old_value: JSONB
└── new_value: JSONB
└── ip_address: String
└── user_agent: String
└── created_at: Timestamp
```

### 18.2 Key Indexes

```sql
CREATE INDEX idx_job_listing_title_company ON job_listing USING gin(to_tsvector('english', title || ' ' || company));
CREATE INDEX idx_job_listing_posted_at ON job_listing(posted_at DESC);
CREATE INDEX idx_application_user_status ON application(user_id, status);
CREATE INDEX idx_notification_user_read ON notification(user_id, read_at);
CREATE INDEX idx_user_email ON user(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_listing_embeddings ON job_listing USING ivfflat (embeddings vector_cosine_ops);
```

### 18.3 Partitioning Strategy

- `audit_log`: Partitioned by month (range partitioning on `created_at`)
- `notification`: Partitioned by month
- `job_listing`: Partitioned by month (range on `scraped_at`) — older data queried less

---

## 19. API Design Philosophy

### 19.1 Standards

- RESTful resource-oriented URLs
- JSON request/response bodies
- Consistent error format
- Versioned via URL prefix: `/api/v1/`
- OpenAPI 3.1 specification (auto-generated with SpringDoc)

### 19.2 API Endpoints (High-Level)

```
# Authentication
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
POST   /api/v1/auth/verify-email

# User Profile
GET    /api/v1/users/me
PUT    /api/v1/users/me
GET    /api/v1/users/me/profile
PUT    /api/v1/users/me/profile
DELETE /api/v1/users/me

# Jobs
GET    /api/v1/jobs (search with filters)
GET    /api/v1/jobs/{id}
POST   /api/v1/jobs/{id}/save
DELETE /api/v1/jobs/{id}/save
GET    /api/v1/jobs/saved
GET    /api/v1/jobs/sources

# Resumes
POST   /api/v1/resumes
GET    /api/v1/resumes
GET    /api/v1/resumes/{id}
PUT    /api/v1/resumes/{id}
DELETE /api/v1/resumes/{id}
POST   /api/v1/resumes/{id}/tailor (AI tailor for job)
POST   /api/v1/resumes/{id}/score (ATS score)
POST   /api/v1/resumes/{id}/export
POST   /api/v1/cover-letters/generate

# Applications
GET    /api/v1/applications
POST   /api/v1/applications
GET    /api/v1/applications/{id}
PUT    /api/v1/applications/{id}/status
DELETE /api/v1/applications/{id}
POST   /api/v1/applications/{id}/automate
GET    /api/v1/applications/{id}/evidence

# Interview
POST   /api/v1/interviews/sessions
GET    /api/v1/interviews/sessions
GET    /api/v1/interviews/sessions/{id}
POST   /api/v1/interviews/sessions/{id}/questions/{qid}/answer
GET    /api/v1/interviews/sessions/{id}/summary

# Career
POST   /api/v1/career/path-analyze
GET    /api/v1/career/paths
GET    /api/v1/career/paths/{id}
GET    /api/v1/career/skills-gap
GET    /api/v1/career/salary-benchmarks

# Networking
POST   /api/v1/networking/messages/generate
POST   /api/v1/networking/campaigns
GET    /api/v1/networking/campaigns
PUT    /api/v1/networking/campaigns/{id}

# Analytics
GET    /api/v1/analytics/dashboard
GET    /api/v1/analytics/funnel
GET    /api/v1/analytics/activity-report

# Notifications
GET    /api/v1/notifications
PUT    /api/v1/notifications/{id}/read
PUT    /api/v1/notifications/read-all
GET    /api/v1/notifications/settings
PUT    /api/v1/notifications/settings

# Admin
GET    /api/v1/admin/users
GET    /api/v1/admin/users/{id}
PUT    /api/v1/admin/users/{id}/role
DELETE /api/v1/admin/users/{id}
GET    /api/v1/admin/system/health
POST   /api/v1/admin/feature-flags
GET    /api/v1/admin/job-sources
POST   /api/v1/admin/job-sources

# Billing
GET    /api/v1/billing/subscription
POST   /api/v1/billing/checkout
POST   /api/v1/billing/portal
GET    /api/v1/billing/invoices
POST   /api/v1/billing/cancel
POST   /api/v1/billing/webhook (Stripe)
```

### 19.3 Response Envelope

```json
{
  "status": "success" | "error",
  "data": { ... },
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Email is required",
    "details": [
      { "field": "email", "message": "must not be blank" }
    ]
  },
  "meta": {
    "page": 1,
    "size": 20,
    "total": 150,
    "timestamp": "2026-07-02T12:00:00Z",
    "request_id": "req_abc123"
  }
}
```

### 19.4 WebSocket Events

```
# Real-time push via STOMP over WebSocket
/topic/applications/{userId}       — Application status changes
/topic/notifications/{userId}      — New notifications
/topic/automation/{userId}         — Automation progress updates
/topic/interviews/{sessionId}      — Live interview session updates
/topic/jobs/{searchId}             — Real-time job match alerts
```

---

## 20. Frontend Architecture

### 20.1 Structure (Next.js 14 App Router)

```
src/
├── app/                              # Routes (App Router)
│   ├── (auth)/                       # Auth group
│   │   ├── login/
│   │   ├── register/
│   │   ├── forgot-password/
│   │   └── reset-password/
│   ├── (dashboard)/                  # Authenticated routes
│   │   ├── dashboard/
│   │   ├── jobs/
│   │   ├── applications/
│   │   ├── resumes/
│   │   ├── interviews/
│   │   ├── career/
│   │   ├── networking/
│   │   ├── analytics/
│   │   ├── settings/
│   │   └── billing/
│   ├── api/                          # API routes (BFF if needed)
│   └── layout.tsx
├── components/
│   ├── ui/                           # shadcn/ui primitives
│   ├── shared/                       # Shared components
│   ├── features/                     # Feature-specific components
│   │   ├── jobs/
│   │   ├── applications/
│   │   ├── resumes/
│   │   ├── interviews/
│   │   └── ...
│   └── layouts/                      # Layout components
├── lib/
│   ├── api/                          # API client (React Query hooks)
│   ├── auth/                         # Auth helpers
│   ├── utils/                        # Utility functions
│   └── validations/                  # Zod schemas
├── hooks/                            # Custom hooks
├── stores/                           # Zustand stores
├── types/                            # TypeScript type definitions
└── styles/                           # Global styles
```

### 20.2 Design System

- Based on shadcn/ui (which uses Radix UI primitives)
- Custom theme with JobPilot brand tokens
- Dark mode via Tailwind `dark:` variant + next-themes
- Consistent spacing, typography, color scales

### 20.3 State Management Strategy

| State Type | Solution | Rationale |
|------------|----------|-----------|
| Server state | React Query | Caching, dedup, optimistic updates, auto-refetch |
| Client state (global) | Zustand | Auth state, UI preferences, currently selected job |
| Client state (local) | React useState/useReducer | Form state, local UI toggles |
| URL state | Next.js searchParams | Filters, pagination, search queries |
| Form state | React Hook Form + Zod | Type-safe, performant, validation |

### 20.4 Performance Strategy

- React Server Components where possible (static content, job listings)
- Streaming SSR for AI-generated content
- Image optimization via Next.js `<Image>` with remote patterns
- Route prefetching for likely navigation
- Bundle analysis and code splitting
- ISR for job listing pages (revalidate every 5 min)

---

## 21. Backend Architecture

### 21.1 Module Structure (Maven Multi-Module)

```
jobpilot/
├── jobpilot-common/                  # Shared: DTOs, utils, constants
├── jobpilot-domain/                  # Domain entities, value objects, domain events
├── jobpilot-application/             # Use cases, application services, ports
├── jobpilot-infrastructure/          # Persistence, messaging, external APIs
├── jobpilot-interfaces/              # REST controllers, WebSocket, DTOs
├── jobpilot-automation/              # Browser automation service
├── jobpilot-ai/                      # AI orchestration service
├── jobpilot-bootstrap/               # Main application entry point
└── jobpilot-gateway/                 # API Gateway (separate deployable)
```

### 21.2 Package Organization (per module, Clean Architecture)

```
com.jobpilot.modules.<module>/
├── domain/
│   ├── model/
│   │   ├── entity/
│   │   ├── valueobject/
│   │   └── aggregate/
│   ├── event/
│   └── service/
├── application/
│   ├── port/
│   │   ├── inbound/    (UseCase interfaces)
│   │   └── outbound/   (Repository interfaces)
│   ├── service/
│   ├── dto/
│   └── mapper/
└── infrastructure/
    ├── persistence/
    │   ├── entity/      (JPA entities)
    │   ├── repository/  (JPA implementations)
    │   └── mapper/      (Domain <-> JPA mapping)
    ├── messaging/
    ├── client/          (REST clients, AI clients)
    └── config/
```

### 21.3 Key Design Patterns

| Pattern | Usage |
|---------|-------|
| Domain Events | Side effects (application submitted → notification, email, analytics) |
| CQRS | Separate read/write models for ATS pipeline and analytics |
| Specification | Reusable query predicates for job search filters |
| Strategy | Pluggable job source adapters, LLM providers, ATS form detectors |
| Factory | Resume builder, application state machine |
| Observer | Notification events, automation progress streaming |
| Repository | Domain-focused persistence abstraction |
| Unit of Work | Transaction management across aggregates |
| Saga | Distributed transaction coordination (payment + subscription activation) |
| Circuit Breaker | Resilience for external API calls (AI providers, job boards) |
| Bulkhead | Isolate AI service resource pools from core API |
| Outbox Pattern | Reliable event publishing (Kafka with outbox table) |

### 21.4 Transaction Management

- `@Transactional` at application service layer for write operations
- Read operations use `@Transactional(readOnly = true)` to optimize connections
- Outbox pattern for Kafka: events go to RDBMS outbox table first, then published by a poller
- Saga for multi-service operations: Payment → Subscription → Email

### 21.5 Exception Handling

- Global `@ControllerAdvice` translating exceptions to consistent error responses
- Domain exceptions in domain layer (not framework-specific)
- Application exceptions for use-case-level errors
- Infrastructure exceptions wrapped in domain-appropriate types

---

## 22. Deployment Strategy

### 22.1 Environment Strategy

| Environment | Purpose | Infrastructure |
|-------------|---------|----------------|
| `local` | Local development | Docker Compose (Postgres, Redis, Kafka) |
| `dev` | Integration testing | Single-node K3s cluster |
| `staging` | Pre-production validation | Multi-node EKS (small instances) |
| `prod` | Production | Multi-node EKS with auto-scaling |

### 22.2 CI/CD Pipeline (GitHub Actions)

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│   Push   │ → │   Build  │ → │   Test   │ → │  Docker  │ → │  Deploy  │
│   PR     │   │  (Maven) │   │ (Unit +  │   │  Build   │   │ (ArgoCD) │
│          │   │          │   │  Integ.) │   │ & Push   │   │          │
└──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────────┘
                                     │
                              ┌──────▼──────┐
                              │  Security   │
                              │  Scan      │
                              │ (Trivy +   │
                              │  OWASP ZAP)│
                              └─────────────┘
```

### 22.3 Container Strategy

- Multi-stage Docker builds for minimal image size
- Base image: `eclipse-temurin:21-jre-alpine`
- Jib plugin for optimized Docker builds (no Docker daemon needed in CI)
- Distroless base for production (no shell, no package manager)
- Separate images for: `gateway`, `core-api`, `automation-worker`, `ai-worker`, `job-aggregator`

### 22.4 Kubernetes Configuration

- Deployments with resource requests/limits
- Horizontal Pod Autoscaler (CPU + memory metrics)
- Pod Disruption Budgets
- Readiness + Liveness probes
- Init containers for DB migration (Flyway)
- ConfigMaps + Secrets for configuration
- Ingress with TLS termination
- Network policies for pod-to-pod communication
- Service meshes (Linkerd) for observability (future)

---

## 23. Scaling Strategy

### 23.1 Horizontal Scaling Dimensions

| Component | Scaling Trigger | Strategy |
|-----------|----------------|----------|
| Core API | CPU > 70% | Add pods (HPA) |
| Automation | Queue depth > 100 | Add worker pods |
| AI Service | Queue depth > 50 | Add worker pods |
| Job Aggregator | Schedule-based | CronJob + worker pool |
| Database | Read replica lag > 1s | Add read replicas |
| Redis | Memory > 80% | Cluster mode sharding |

### 23.2 Database Scaling

- **Read replicas:** 1→3→N based on query load
- **Connection pooling:** HikariCP with max 200 connections per instance
- **PgBouncer:** Transaction pooling for ephemeral connections
- **Read/write splitting:** Spring's `@Transactional(readOnly = true)` → replica
- **Partitioning:** Time-based partitions for audit_log, notifications, job_listings
- **Archival:** Jobs older than 12 months → cold storage (S3 + Parquet)
- **Future:** Citus-style sharding if single Postgres becomes bottleneck

### 23.3 Caching Strategy

| Cache | Key | TTL | Location | Purpose |
|-------|-----|-----|----------|---------|
| Job listings | `job:{id}` | 5 min | Redis | Reduce DB reads |
| Search results | `search:{query_hash}` | 2 min | Redis | Avoid recomputation |
| User session | `session:{token}` | 15 min | Redis | JWT revocation |
| AI responses | `ai:{prompt_hash}` | 24h | Redis | Reduce API costs |
| Rate limit | `ratelimit:{user_id}` | Dynamic | Redis | Sliding window |
| User profile | `profile:{user_id}` | 10 min | Redis | Frequent reads |

### 23.4 Message Queue Scaling

- Kafka partitions: N = max(consumers) × 2 for headroom
- Consumer groups: `automation-group`, `ai-group`, `notification-group`, `analytics-group`
- Retry topics with exponential backoff (1s, 5s, 30s, 5min, 30min → DLQ)

---

## 24. Testing Strategy

### 24.1 Test Pyramid

```
            ╱╲
           ╱  ╲          E2E (Playwright Test)
          ╱    ╲         ~5% of tests
         ╱──────╲
        ╱        ╲       Integration (Testcontainers)
       ╱          ╲      ~15% of tests
      ╱────────────╲
     ╱              ╲    Unit (JUnit 5 + Mockito)
    ╱                ╲   ~80% of tests
   ╱──────────────────╲
```

### 24.2 Testing Layers

| Layer | Tool | Scope | Target Coverage |
|-------|------|-------|-----------------|
| Unit | JUnit 5 + AssertJ + Mockito | Domain entities, value objects, domain services, application use cases | 90%+ (domain), 80%+ (application) |
| Integration | Testcontainers + WireMock | Repository implementations, AI adapter, external API clients, message publishing | 90%+ |
| API | MockMvc + WebTestClient | REST endpoints, validation, error handling, security | 90%+ |
| Component | @SpringBootTest | Full module interaction, database operations, message handling | 70%+ |
| E2E | Playwright Test + Testcontainers | Critical user journeys, happy paths, edge cases | 100% of P0 scenarios |
| Performance | k6 | API throughput, latency, connection pooling, DB query performance | P95 targets |
| Security | OWASP ZAP + Trivy | Dependency scanning, DAST, SAST | Zero critical/high |
| Architecture | ArchUnit | Layer dependency rules, naming conventions, package cycles | CI enforcement |

### 24.3 What to Test (Critical Paths)

**P0 (Must pass before deploy):**
- User registration + authentication flow
- Job search + application pipeline
- Resume upload + AI tailoring
- Automated application submission
- Payment checkout flow
- Data export + account deletion

**P1 (Test nightly):**
- Career path analysis
- Interview practice
- Networking message generation
- Analytics dashboard

**P2 (Test weekly):**
- Admin console operations
- Bulk operations
- Notification delivery (email + push)

### 24.4 Test Data Management

- Factory pattern for test data generation
- Testcontainers for throwaway database per test class
- Test JSON fixtures for API responses (WireMock)
- Database test lifecycle: `@BeforeAll` setup → `@AfterEach` cleanup
- Separate test profiles (`application-test.yml`) with in-memory config

---

## 25. Monitoring & Observability

### 25.1 Three Pillars of Observability

| Pillar | Tool | Metrics |
|--------|------|---------|
| Metrics | Prometheus + Grafana | RED metrics (Rate, Errors, Duration) |
| Logs | Loki / ELK + Grafana | Structured JSON logs, log levels |
| Traces | OpenTelemetry + Jaeger | Request tracing across services |

### 25.2 Key Metrics (RED)

| Metric | Description | Target |
|--------|-------------|--------|
| `http_requests_total` | Total requests by endpoint, method, status | N/A |
| `http_request_duration_ms` | Request latency p50, p95, p99 | < 200ms p50, < 500ms p95 |
| `error_rate` | Percentage of 5xx responses | < 0.1% |
| `automation_success_rate` | % of successful automated applications | > 95% |
| `ai_response_time` | AI provider response time | < 5s p95 |
| `db_query_duration` | Database query latency | < 50ms p95 |
| `cache_hit_ratio` | Redis cache hit rate | > 80% |
| `queue_depth` | Kafka consumer lag per topic | < 1000 |
| `active_users` | Concurrent active users | Monitor growth |

### 25.3 Dashboards

1. **Executive Dashboard:** MAU, revenue, subscription counts, application volume
2. **Operations Dashboard:** Service health, error rates, latency, resource usage
3. **Business Dashboard:** Application funnel, conversion rates, success metrics
4. **Automation Dashboard:** Success rate, CAPTCHA rate, throughput, errors by ATS type
5. **AI Dashboard:** Token usage, cost, response times, provider breakdown

### 25.4 Alerting Rules (Prometheus AlertManager)

| Alert | Condition | Severity |
|-------|-----------|----------|
| High Error Rate | `error_rate > 1% for 5m` | Critical |
| High Latency | `p95_latency > 2s for 5m` | Warning |
| Service Down | `up == 0 for 30s` | Critical |
| DB Connection Pool Exhausted | `hikaricp_connections_active == max for 1m` | Critical |
| Automation Failure Spike | `automation_success_rate < 80% for 10m` | Critical |
| AI Cost Spike | `ai_cost_per_hour > $50` | Info |
| Queue Backlog | `kafka_consumer_lag > 10000` | Warning |

### 25.5 Health Checks

- `/actuator/health` — Liveness + Readiness
- `/actuator/info` — Build info, commit SHA
- `/actuator/metrics` — JVM, DB pool, HTTP, cache
- Custom health indicators for: DB, Redis, Kafka, AI provider, automation nodes

---

## 26. Logging Strategy

### 26.1 Structure

All logs are structured JSON with mandatory fields:

```json
{
  "timestamp": "2026-07-02T12:00:00.000Z",
  "level": "INFO",
  "logger": "com.jobpilot.modules.application.service.ApplicationService",
  "thread": "http-nio-8080-exec-3",
  "trace_id": "abc123def456",
  "span_id": "ghi789",
  "user_id": "usr_xyz",
  "request_id": "req_abc123",
  "message": "Application submitted successfully",
  "context": {
    "application_id": "app_001",
    "job_listing_id": "job_002",
    "automation_status": "SUBMITTED",
    "duration_ms": 4500
  }
}
```

### 26.2 Log Levels

| Level | Usage |
|-------|-------|
| `ERROR` | Unhandled exceptions, integration failures, business rule violations |
| `WARN` | Degraded performance, retries, rate limiting, suspicious activity |
| `INFO` | Business events (user registered, application submitted, payment received) |
| `DEBUG` | Detailed flow for troubleshooting (disabled in production by default) |
| `TRACE` | Step-by-step automation actions (only for automation service, rotated frequently) |

### 26.3 Tools

- **Production:** Logback → JSON appender → stdout → Loki
- **Dynamic log level:** `/actuator/loggers` for on-the-fly debugging
- **Sensitive data masking:** Logback custom converter to redact PII (emails, tokens, passwords)
- **Audit log:** Separate database table (not file) for compliance-sensitive events

### 26.4 Retention

| Log Type | Retention | Storage |
|----------|-----------|---------|
| Application logs | 30 days | Loki |
| Audit logs | 7 years (compliance) | Postgres + S3 archive |
| Error logs | 90 days | Loki + Sentry |
| Automation logs | 7 days | Loki (high volume) |
| Access logs (API Gateway) | 90 days | S3 |

---

## 27. Documentation Strategy

### 27.1 Documentation Types

| Document | Audience | Format | Location |
|----------|----------|--------|----------|
| SRS (this document) | All stakeholders | Markdown | `docs/srs.md` |
| Architecture Decision Records | Engineers | Markdown (ADR) | `docs/adr/` |
| API Reference | Developers | OpenAPI 3.1 (interactive) | `/swagger-ui.html` |
| Setup Guide | Developers | Markdown | `README.md` |
| Deployment Guide | DevOps | Markdown | `docs/deployment.md` |
| User Guide | End users | Web (Next.js pages) | `/help` route |
| Contribution Guide | Contributors | Markdown | `CONTRIBUTING.md` |
| Runbook | On-call engineers | Markdown | `docs/runbook.md` |
| Security Policy | Security team | Markdown | `SECURITY.md` |

### 27.2 ADR Format (MADR)

Each ADR includes: Title, Status, Context, Decision, Consequences, Compliance.

Decision records for: technology choices, architectural patterns, module boundaries, naming conventions, dependency injection approach, exception handling strategy, testing approach.

---

## 28. Development Roadmap

### Phase 0 — Foundation (Weeks 1–4)
- [ ] Repository setup (GitHub, branch protection, conventions)
- [ ] Maven multi-module project scaffold
- [ ] Domain entities + value objects
- [ ] Database schema + Flyway migrations
- [ ] Docker Compose for local dev
- [ ] CI pipeline (build + test)
- [ ] API Gateway setup
- [ ] Authentication + Authorization (JWT + OAuth)
- [ ] User registration + login flows
- [ ] User profile CRUD

### Phase 1 — Core MVP (Weeks 5–10)
- [ ] Job listing schema + aggregation from 3 sources (LinkedIn, Indeed, Google Jobs)
- [ ] Full-text search + filtering
- [ ] Resume builder (manual + basic AI)
- [ ] Cover letter generator (basic AI)
- [ ] ATS pipeline (Kanban CRUD)
- [ ] Manual application tracking
- [ ] Dashboard (basic metrics)
- [ ] Notification system (in-app + email)
- [ ] Subscription management + Stripe integration
- [ ] WebSocket for real-time updates

### Phase 2 — AI & Automation (Weeks 11–18)
- [ ] AI provider abstraction layer
- [ ] Resume AI tailoring (full)
- [ ] ATS scoring engine
- [ ] Interview question prediction
- [ ] Mock interview simulator (text mode)
- [ ] Career path analysis
- [ ] Skills gap analysis
- [ ] Browser automation module (Playwright Java)
- [ ] Form detection engine
- [ ] ATS-specific adapters (Greenhouse, Lever, Workday)
- [ ] Automated application submission
- [ ] Automation dashboard + monitoring

### Phase 3 — Intelligence (Weeks 19–24)
- [ ] Semantic job search (pgvector)
- [ ] Salary benchmarking
- [ ] Market trend analysis
- [ ] Interview voice mode (speech-to-text)
- [ ] Answer scoring + feedback
- [ ] Networking message generator
- [ ] Outreach campaign manager
- [ ] Analytics engine (funnel, conversion, reports)
- [ ] Admin console
- [ ] Data export (GDPR)

### Phase 4 — Scale (Weeks 25–32)
- [ ] Performance optimization (caching, query tuning)
- [ ] Load testing + bottleneck resolution
- [ ] Database partitioning + archiving
- [ ] Read replica setup + read/write splitting
- [ ] Auto-scaling configuration
- [ ] SOC 2 compliance readiness
- [ ] Additional ATS adapters (Taleo, BambooHR, SuccessFactors)
- [ ] 10+ more job source adapters
- [ ] Anti-detection improvements
- [ ] Mobile app (React Native) — initial release
- [ ] Enterprise features (team accounts, shared pipelines)

---

## 29. Future Scope

| Feature | Description | Priority |
|---------|-------------|----------|
| AI Application Screening (Recruiter Side) | Let recruiters view candidate skill reports | Medium |
| Resume + JD Matching Score | Public API for companies to score candidates | Low |
| Auto-Follow Up | AI composes and sends follow-up emails | High |
| Salary Negotiation Coach | AI-powered negotiation strategy | Medium |
| LinkedIn Auto-Engage | Like, comment, connect based on strategy | Low |
| Team/Collaborative Job Search | Shared pipelines for recruiting teams | Medium |
| AI Video Interview Practice | Record video, analyze body language + tone | High |
| Browser Extension | Auto-save jobs from any site | Medium |
| Mobile App (React Native) | Full mobile experience | High |
| Company Pages | Aggregated company insights, salary, reviews | Low |
| Marketplace | Career coaches, resume writers, interview prep services | Medium |
| White-Label | Agency/company branded career platforms | Low |
| Offline Mode | PWA with offline capabilities | Low |

---

## 30. Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Job boards block scraping/automation | High | High | Use residential proxies, rotate user agents, respect rate limits, provide manual fallback |
| AI API cost overruns | Medium | Medium | Token budgeting per user tier, caching, fallback to smaller models, usage dashboards |
| LLM hallucinations in critical data (salary, suggestions) | Medium | Medium | Data-first approach for structured data, LLM only for text generation, human review prompts |
| Browser automation breaks on ATS updates | High | High | Screenshot-based testing, automated regression suite, graceful degradation, manual override |
| PostgreSQL becomes bottleneck at scale | Medium | Medium | Read replicas, partitioning, connection pooling, plan for Citus/read-only replicas, caching |
| User churn due to subscription pricing | Medium | High | Generous free tier, clear value prop, usage-based upgrades, onboarding conversions |
| Security breach (user data exposure) | Critical | Low | Defense in depth, encryption at rest/transit, regular pen tests, SOC 2 compliance |
| Regulatory compliance (GDPR/CCPA) | High | Medium | Privacy-by-design, data mapping, consent management, DPA with cloud providers |
| OAuth provider API changes | Medium | Low | OAuth 2.0 standard, multiple providers, password fallback |
| Competition (LinkedIn AI features) | Medium | Medium | Stay niche (multi-board automation, resume tailoring), innovate faster, build community |

---

## 31. Success Metrics

### 31.1 Product Metrics

| Metric | Target (Q1) | Target (Q2) | Target (Year 1) |
|--------|-------------|-------------|-----------------|
| Monthly Active Users (MAU) | 1,000 | 5,000 | 50,000 |
| Paid Conversion Rate | 5% | 8% | 12% |
| Monthly Recurring Revenue (MRR) | $2,000 | $20,000 | $250,000 |
| Average Revenue Per User (ARPU) | $10/month | $12/month | $15/month |
| User Retention (D30) | 40% | 50% | 60% |
| Net Promoter Score (NPS) | 30 | 40 | 50+ |
| Automated Applications Submitted | 500/month | 5,000/month | 100,000/month |
| Automation Success Rate | 85% | 92% | 95%+ |

### 31.2 Engineering Metrics

| Metric | Target |
|--------|--------|
| API p95 latency | < 500ms |
| Uptime | 99.9% |
| Test Coverage | 80%+ (core), 60%+ (overall) |
| Deployment Frequency | Multiple times per week |
| Mean Time to Recovery (MTTR) | < 1 hour |
| Mean Time Between Failures (MTBF) | > 30 days |
| Bug Escape Rate | < 5% of total bugs found |

### 31.3 Quality Metrics

| Metric | Target |
|--------|--------|
| E2E Test Pass Rate | 100% |
| Critical Security Vulnerabilities | 0 |
| Lighthouse Score (Performance) | 90+ |
| Lighthouse Score (Accessibility) | 85+ |
| Lighthouse Score (Best Practices) | 95+ |
| WCAG 2.1 AA Compliance | Full |

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| ATS | Applicant Tracking System |
| CQRS | Command Query Responsibility Segregation |
| BFF | Backend For Frontend |
| CAPTCHA | Completely Automated Public Turing test to tell Computers and Humans Apart |
| Clean Architecture | Architecture pattern by Robert C. Martin emphasizing separation of concerns |
| DDD | Domain-Driven Design |
| DLQ | Dead Letter Queue |
| IaC | Infrastructure as Code |
| JWT | JSON Web Token |
| LLM | Large Language Model |
| MRR | Monthly Recurring Revenue |
| PII | Personally Identifiable Information |
| RED | Rate, Errors, Duration (monitoring methodology) |
| SRS | Software Requirements Specification |
| SSO | Single Sign-On |
| WAF | Web Application Firewall |

## Appendix B: Reference Technologies Versions

| Technology | Version |
|------------|---------|
| Java | 21 LTS |
| Spring Boot | 3.3.x |
| Spring Security | 6.3.x |
| Spring Cloud Gateway | 4.1.x |
| PostgreSQL | 16 |
| Redis | 7.2 |
| Kafka | 3.7 |
| Flyway | 10.x |
| Playwright Java | 1.45+ |
| Next.js | 14.2+ |
| TypeScript | 5.4+ |
| Docker | 26+ |
| Kubernetes | 1.30+ |
| Terraform | 1.8+ |

---

*This document is a living artifact. It should be reviewed and updated as the project evolves and new information emerges. All architectural decisions should be recorded as ADRs in `docs/adr/`.*

---

**End of SRS v1.0**
