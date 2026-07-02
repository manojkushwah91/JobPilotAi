# GitHub Milestones & Issues

## Milestone 1: Platform Foundation (68 SP)
*Foundational infrastructure — every future module depends on this*

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M1-01 | Critical | 5 | Parent Maven POM + multi-module project setup |
| M1-02 | Critical | 8 | Common module (BaseException, ApiResponse, IdGenerator, TimeProvider) |
| M1-03 | Critical | 8 | Domain module (BaseAggregateRoot, BaseEntity, BaseValueObject, DomainEvent) |
| M1-04 | Critical | 13 | Infrastructure module (JPA config, Flyway setup, Redis, Kafka, S3 adapters) |
| M1-05 | Critical | 8 | Interfaces module (GlobalExceptionHandler, OpenAPI config, shared DTOs) |
| M1-06 | Critical | 5 | Bootstrap module (@SpringBootApplication, ModuleConfig, health endpoints) |
| M1-07 | High | 5 | Docker Compose for all infrastructure services |
| M1-08 | High | 3 | Flyway migrations V1-V7 (all schema + seed data) |
| M1-09 | High | 3 | GitHub Actions CI (build, test, lint, security scan) |
| M1-10 | High | 3 | Code quality tools (Checkstyle, SpotBugs, PMD, JaCoCo, EditorConfig) |
| M1-11 | Medium | 3 | Observability (Micrometer, Prometheus, Grafana dashboards, Loki, Tempo) |
| M1-12 | Medium | 2 | Dev scripts (start-dev, stop-dev, reset-dev, seed-db) |
| M1-13 | Medium | 2 | Pre-commit hooks + commit convention documentation |

## Milestone 2: Authentication & Identity (34 SP)
*Secure user management, JWT, OAuth, RBAC infrastructure*

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M2-01 | Critical | 8 | JWT RS256 infrastructure (token generation, validation, JWKS endpoint, rotation) |
| M2-02 | Critical | 8 | OAuth 2.0 adapters (Google, LinkedIn, GitHub, Microsoft) |
| M2-03 | Critical | 5 | RBAC framework (4 tiers: FREE/PREMIUM/PRO/ADMIN, @PreAuthorize annotations) |
| M2-04 | High | 5 | User aggregate (registration, email verification, password hashing) |
| M2-05 | High | 3 | Refresh token rotation with theft detection |
| M2-06 | Medium | 3 | CORS configuration + Content Security Policy headers |
| M2-07 | Medium | 2 | Rate limiting infrastructure (Bucket4j + Redis, per-endpoint groups) |

## Milestone 3: User & Profile (21 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M3-01 | Critical | 8 | UserProfile entity + REST API (CRUD, avatar upload) |
| M3-02 | High | 5 | UserSettings entity (job preferences, notification prefs, privacy) |
| M3-03 | High | 5 | GDPR data export (generate downloadable JSON) |
| M3-04 | Medium | 3 | Account deletion (soft delete + data anonymization) |

## Milestone 4: Resume Studio (34 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M4-01 | Critical | 8 | Resume aggregate root + sections (CRUD, ordering, versioning) |
| M4-02 | Critical | 8 | ResumeParser (PDF/DOCX/TXT → structured sections) |
| M4-03 | High | 8 | ATS Scoring Engine (keyword extraction, structure analysis, format scoring) |
| M4-04 | High | 5 | Resume PDF/DOCX export |
| M4-05 | Medium | 3 | Drag-and-drop section reorder UI |
| M4-06 | Medium | 2 | Version history viewer + rollback |

## Milestone 5: Job Discovery (34 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M5-01 | Critical | 8 | Job source adapters (Indeed, LinkedIn, Google Jobs, Glassdoor) |
| M5-02 | Critical | 8 | Job aggregation scheduler + dedup |
| M5-03 | High | 8 | Full-text search (tsvector GIN index) + semantic search (pgvector) |
| M5-04 | High | 5 | Faceted search + filters (location, salary, type, experience) |
| M5-05 | Medium | 3 | Saved searches + job alerts |
| M5-06 | Medium | 2 | Match score badge on job cards |

## Milestone 6: Application Management (26 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M6-01 | Critical | 8 | Application aggregate + state machine (9-status lifecycle) |
| M6-02 | High | 5 | Kanban board UI with drag-and-drop status changes |
| M6-03 | High | 5 | Notes + attachments per application |
| M6-04 | Medium | 5 | Follow-up reminders + timeline |
| M6-05 | Medium | 3 | Cover letter generation flow integration |

## Milestone 7: Company Intelligence (21 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M7-01 | High | 8 | Company profile enrichment (LinkedIn, Glassdoor, Crunchbase, Levels.fyi) |
| M7-02 | High | 5 | Tech stack detection (Wappalyzer-style analysis) |
| M7-03 | Medium | 5 | Salary benchmarks by role/location |
| M7-04 | Medium | 3 | Hiring trends visualization |

## Milestone 8: Interview Hub (29 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M8-01 | Critical | 8 | AI question prediction per role/company |
| M8-02 | Critical | 8 | Answer scoring with structured feedback |
| M8-03 | High | 8 | Mock interview session flow (text mode) |
| M8-04 | Medium | 5 | Voice mode integration (browser speech APIs) |

## Milestone 9: Career Analytics (21 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M9-01 | High | 8 | Application funnel metrics + conversion rates |
| M9-02 | High | 5 | Skill gap analysis + recommendations |
| M9-03 | Medium | 5 | Resume score trend over time |
| M9-04 | Medium | 3 | Activity streak + engagement metrics |

## Milestone 10: AI Platform (26 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M10-01 | Critical | 5 | Multi-provider abstraction (OpenAI, Anthropic, Ollama, Gemini adapters) |
| M10-02 | Critical | 5 | Prompt engine (templates, versioning, variable resolution, seeding) |
| M10-03 | High | 5 | Circuit breaker + provider fallback (Resilience4j-based) |
| M10-04 | High | 5 | Token tracking + cost analytics dashboard |
| M10-05 | Medium | 3 | Response caching (L1: Caffeine, L2: Redis) |
| M10-06 | Medium | 3 | A/B testing framework for prompt versions |

## Milestone 11: Browser Automation (21 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M11-01 | Critical | 8 | Playwright Java engine (headless Chrome, stealth mode) |
| M11-02 | High | 8 | ATS form detection + form filling engine |
| M11-03 | High | 5 | Portal adapters (LinkedIn, Indeed, Workday, Greenhouse, Lever) |

## Milestone 12: Production Readiness (21 SP)

| Issue | Priority | SP | Description |
|-------|----------|----|-------------|
| M12-01 | High | 5 | Performance optimization (query tuning, caching, compression) |
| M12-02 | High | 5 | Security audit + penetration testing |
| M12-03 | Medium | 5 | Load testing (k6/Gatling, 1000 concurrent users) |
| M12-04 | Medium | 3 | Documentation finalization |
| M12-05 | Medium | 3 | Production deployment + monitoring handoff |

**Total: 356 Story Points**
