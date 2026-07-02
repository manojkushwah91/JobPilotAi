# Repository Structure

```
JobPilotAI/
│
├── backend/                          # Java 21, Spring Boot 3, Maven multi-module
│   ├── pom.xml                       # Parent POM — dependency management, plugin config
│   ├── jobpilot-bootstrap/           # @SpringBootApplication entry point
│   ├── jobpilot-common/              # Shared kernel: exceptions, VOs, utils (ZERO deps)
│   ├── jobpilot-domain/              # Pure domain: entities, aggregates, ports
│   ├── jobpilot-application/         # Use cases, application services, inbound/outbound ports
│   ├── jobpilot-infrastructure/      # Adapters: JPA, Redis, Kafka, AI, email, security
│   ├── jobpilot-interfaces/          # REST controllers, DTOs, WebSocket, exception handlers
│   └── modules/                      # Bounded contexts — each extractable to microservice
│       ├── identity/                 # Authentication, authorization, user management
│       ├── resume/                   # Resume studio, ATS scoring
│       ├── job/                      # Job discovery, matching
│       ├── application/              # Application tracker, Kanban
│       ├── interview/                # Interview hub, mock sessions
│       ├── company/                  # Company intelligence
│       ├── analytics/                # Career analytics, dashboards
│       ├── notification/             # Multi-channel notification engine
│       ├── automation/               # Browser automation (separate deployable)
│       ├── ai/                       # AI provider layer, prompt engine
│       ├── billing/                  # Subscription, payments, invoices
│       ├── admin/                    # Admin panel, audit, feature flags
│       └── search/                   # Full-text + semantic search engine
│
├── frontend/                         # Next.js 14, App Router, TypeScript, shadcn/ui
│   ├── src/
│   │   ├── app/                      # App Router pages
│   │   │   ├── (auth)/               # Login, register, OAuth callback
│   │   │   ├── (dashboard)/          # All authenticated pages
│   │   │   └── (marketing)/          # Landing, pricing, about
│   │   ├── components/               # Shared UI + feature components
│   │   │   ├── ui/                   # shadcn/ui primitives
│   │   │   ├── layout/               # Navbar, Sidebar, Footer, Shell
│   │   │   └── features/             # Domain-specific composable components
│   │   ├── lib/                      # API client, auth, hooks, utils, store
│   │   ├── types/                    # TypeScript type definitions
│   │   └── styles/                   # Global styles, design tokens
│   ├── public/                       # Static assets
│   ├── package.json
│   └── next.config.js
│
├── automation/                       # Playwright Java — standalone deployable
│
├── infrastructure/                   # Docker, K8s, Terraform, monitoring
│   ├── docker/                       # Docker Compose per environment
│   ├── kubernetes/                   # Kustomize overlays per environment
│   ├── terraform/                    # Infrastructure as Code
│   └── monitoring/                   # Prometheus, Grafana, Loki, Tempo config
│
├── database/                         # Flyway migrations, seeders, rollbacks
│   ├── migrations/                   # Per-environment migration sets
│   ├── seeders/                      # Development seed data
│   └── rollbacks/                    # Manual rollback scripts
│
├── .github/                          # CI/CD, issue templates
│   ├── workflows/                    # GitHub Actions pipelines
│   └── ISSUE_TEMPLATE/              # Standardized issue creation
│
├── docs/                             # Living documentation
│   ├── architecture/                 # ADRs, C4, diagrams
│   ├── decisions/                    # Architecture Decision Records
│   ├── api/                          # OpenAPI specs
│   └── guides/                       # Developer guides, runbooks
│
├── scripts/                          # Developer tooling
│   ├── dev/                          # start-dev, stop-dev, reset-dev
│   ├── db/                           # seed-db, migrate, rollback
│   └── ci/                           # CI helper scripts
│
├── shared/                           # Cross-project shared artifacts
│   ├── proto/                        # Protobuf definitions
│   └── kafka-schemas/               # AVRO/JSON schemas for events
│
├── tools/                            # Engineering tooling
│   ├── load-testing/                 # Gatling/k6 scenarios
│   ├── chaos-engineering/            # Chaos Monkey experiments
│   └── data-generator/              # Synthetic data generation
│
├── .editorconfig
├── .gitignore
├── .pre-commit-config.yaml
├── checkstyle.xml
├── Makefile
└── README.md
```

## Rationale

| Directory | Purpose |
|-----------|---------|
| `backend/` | Modular monolith with Clean Architecture. Each module in `modules/` is a bounded context with its own domain/application/infrastructure layers. Extractable to microservices via the extraction triggers defined in HLD. |
| `frontend/` | Next.js 14 App Router with SSR/ISR/CSR per route. Feature-based component organization inside `components/features/`. |
| `automation/` | Playwright Java browser automation. Standalone deployable — separated because it has heavy dependency (Chromium) and different scaling needs. |
| `infrastructure/` | Everything DevOps: Docker, Kubernetes (Kustomize), Terraform, monitoring configs. Environment overlays for dev/staging/prod. |
| `database/` | Flyway migrations with per-environment folders. Seed data for local development. Rollback scripts for production emergencies. |
| `shared/` | Protobuf definitions and Kafka AVRO schemas — the contract between services when bounded contexts are eventually extracted. |
| `tools/` | Engineering excellence: load testing, chaos engineering, data generators for performance testing. |
| `scripts/` | Developer productivity: one-command setup, database management, CI helpers. |
| `docs/` | Living documentation: ADRs track every architectural decision, C4 diagrams show the system, OpenAPI specs define the API contract, runbooks document incident response. |
