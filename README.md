# JobPilot AI 🚀

> AI-powered job search platform — automate applications, track progress, and land your dream job.

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)]()
[![Next.js](https://img.shields.io/badge/Next.js-14-black)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

## ✨ Features

| Module | Description |
|--------|-------------|
| **🔐 Authentication** | JWT-based auth with refresh tokens, RBAC, BCrypt, token revocation |
| **📝 Resumes** | Rich resume builder with ATS scoring, versioning, section editor |
| **💼 Job Listings** | Search, filter, full-text search, salary ranges, saved jobs |
| **📋 Applications** | Kanban board, 9-status workflow, notes, status history timeline |
| **🏢 Companies** | Company profiles, funding rounds, salary data, hiring trends |
| **🎤 Interviews** | Live Q&A flow with AI scoring, scheduling, session management |
| **🔔 Notifications** | Multi-channel (in-app/email), read tracking, real-time toasts |
| **🤖 AI Services** | Resume scoring, skill gap analysis, job matching (OpenAI + mock) |
| **📊 Analytics** | Dashboards with funnel charts, trends, AI usage metrics |
| **💰 Billing** | Subscription plans, upgrade/cancel lifecycle, invoice history |
| **🔧 Admin** | Feature flags, audit logs, user management |
| **⚡ Automation** | Playwright-based job application automation engine |
| **📄 Cover Letters** | AI-generated cover letters with tone selection |
| **🔒 Security** | Rate limiting, CSRF, CSP headers, Redis token store |

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│                   Frontend                       │
│            Next.js 14 + TypeScript               │
│     Tailwind CSS + Radix UI + React Query        │
└────────────────┬────────────────────────────────┘
                 │ REST API (JWT)
┌────────────────┴────────────────────────────────┐
│               Backend (Monolith)                 │
│    Spring Boot 3.2 + Java 21 + Hexagonal Arch    │
│                                                  │
│  ┌─────────┐ ┌──────────┐ ┌────────────────┐   │
│  │ Domain  │ │Application│ │ Infrastructure │   │
│  │ (POJOs) │ │ (UseCases)│ │ (JPA, Redis,   │   │
│  │         │ │ (Services)│ │  OpenAI, Mail)  │   │
│  └─────────┘ └──────────┘ └────────────────┘   │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────┴────────────────────────────────┐
│            Data Layer                            │
│    PostgreSQL + Redis + MinIO (S3) + Kafka      │
└─────────────────────────────────────────────────┘
```

## 🛠 Tech Stack

### Backend
- **Java 21** with **Spring Boot 3.2**
- **Hexagonal Architecture** (Domain → Application → Infrastructure → Interfaces)
- **PostgreSQL** with Flyway migrations
- **Redis** for caching and token revocation
- **OpenAI GPT** integration with mock fallback
- **Playwright** for browser automation
- **JWT** with refresh token rotation

### Frontend
- **Next.js 14** (App Router)
- **TypeScript** with strict mode
- **Tailwind CSS** for styling
- **Radix UI** for accessible primitives
- **React Query** for data fetching
- **Zustand** for state management
- **Recharts** for analytics dashboards
- **Sonner** for toast notifications

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Node.js 20+
- Docker (for PostgreSQL, Redis)

### 1. Clone & Setup
```bash
git clone https://github.com/manojkushwah91/JobPilotAi.git
cd JobPilotAi
```

### 2. Start Infrastructure
```bash
docker compose -f docker-compose.yml up -d postgres redis
```

### 3. Run Backend
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Run Frontend
```bash
cd frontend
npm install
npm run dev
```

### 5. Open Browser
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html

## 📦 Project Structure

```
JobPilotAi/
├── backend/
│   ├── jobpilot-common/          # Shared utilities & exceptions
│   ├── jobpilot-domain/          # Domain aggregates, value objects, events
│   ├── jobpilot-application/     # Use cases, services, DTOs, ports
│   ├── jobpilot-infrastructure/  # JPA, Redis, OpenAI, Mail, Security
│   ├── jobpilot-interfaces/      # REST controllers, filters, annotations
│   └── jobpilot-bootstrap/       # Main application entry point
│       └── src/main/resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── db/migration/dev/  # Flyway migrations (V1-V7)
├── frontend/
│   ├── src/
│   │   ├── app/                  # Next.js 14 App Router pages (27 routes)
│   │   ├── components/
│   │   │   ├── ui/               # 21 reusable UI components
│   │   │   └── features/         # Feature-specific components
│   │   ├── lib/
│   │   │   ├── api/              # Axios client + endpoint config
│   │   │   └── auth/             # Auth provider + Zustand store
│   │   └── types/                # TypeScript type definitions
│   ├── __tests__/                # Jest unit tests
│   └── e2e/                      # Playwright E2E tests
├── monitoring/
│   ├── grafana/                  # Grafana dashboards + datasources
│   ├── prometheus.yml            # Prometheus scrape config
│   └── docker-compose.monitoring.yml
├── docker-compose.yml            # Production services
└── Dockerfile                    # Backend & Frontend Dockerfiles
```

## 🧪 Testing

```bash
# Backend (121 tests)
cd backend && mvn test

# Frontend unit tests
cd frontend && npm test

# E2E tests
cd frontend && npm run e2e
```

## 📊 Monitoring

```bash
docker compose -f monitoring/docker-compose.monitoring.yml up -d
# Grafana: http://localhost:3001 (admin/admin)
# Prometheus: http://localhost:9090
```

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

MIT — see [LICENSE](LICENSE).
