# JobPilot AI

> Autonomous AI Job Agent — The AI applies to jobs 24/7 while you supervise.

[![Build](https://img.shields.io/github/actions/workflow/status/manojkushwah91/JobPilotAi/build.yml?branch=main&label=build)](https://github.com/manojkushwah91/JobPilotAi/actions)
[![Tests](https://img.shields.io/github/actions/workflow/status/manojkushwah91/JobPilotAi/build.yml?branch=main&label=tests)](https://github.com/manojkushwah91/JobPilotAi/actions)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?logo=next.js&logoColor=white)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## What is JobPilot AI?

JobPilot AI is a fully autonomous AI-powered job application agent. You sign up, fill your profile once, and the AI takes over — scraping jobs from multiple boards, scoring match quality, tailoring your resume per job, generating cover letters, filling application forms via browser automation, and monitoring your email for responses.

**The AI applies. You supervise.**

## Features

### AI-Powered Job Intelligence
| Feature | Description |
|---------|-------------|
| **Job Match Scoring** | AI scores every job 0-100 based on skills, experience, education, location, and salary fit |
| **Resume Tailoring** | AI rewrites your resume to match each job's keywords and requirements |
| **Cover Letter Generation** | AI writes personalized cover letters for each application |
| **ATS Resume Analyzer** | Analyzes your resume against job descriptions for ATS compatibility |
| **Interview Preparation** | Generates technical/behavioral questions, STAR method answers, and strategy |
| **Company Intelligence** | Researches company culture, financials, competitors, and interview insights |

### Autonomous Browser Automation
| Feature | Description |
|---------|-------------|
| **Multi-Board Scraping** | Scrapes jobs from Indeed, LinkedIn with full descriptions |
| **Portal Detection** | Auto-detects 16 portal types (Greenhouse, Lever, Workday, etc.) |
| **Smart Form Filling** | Portal-specific selectors for accurate form completion |
| **Cookie Persistence** | Saves sessions per portal to avoid re-login |
| **CAPTCHA Handling** | Detects CAPTCHAs and pauses for manual resolution |
| **Stealth Mode** | User agent rotation, anti-detection, random delays |

### Agent Runtime
| Feature | Description |
|---------|-------------|
| **Autonomous Loop** | Observe → Score → Tailor → Apply → Verify → Learn |
| **Mission Control** | Define goals, set daily limits, monitor progress |
| **Long-Term Memory** | Agent learns from successes and failures across sessions |
| **Email Monitoring** | Detects interview invites, rejections, and offers |
| **Real-Time Chat** | Natural language control: "Find remote Java jobs", "Pause until tomorrow" |

### Production Infrastructure
| Feature | Description |
|---------|-------------|
| **CI/CD Pipeline** | GitHub Actions: build, test, security scan, release |
| **Dockerized** | Multi-stage Dockerfiles, docker-compose for dev and prod |
| **Monitoring** | Prometheus + Grafana dashboards, structured logging |
| **API Docs** | SpringDoc OpenAPI with Swagger UI |
| **Architecture** | C4 Architecture diagrams (Level 1-4) |

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                   Mission Control (Frontend)              │
│            Next.js 14 + TypeScript + Tailwind CSS         │
│  Dashboard | Agent Chat | Applications | Resumes | Settings│
└──────────────────────────┬───────────────────────────────┘
                           │ WebSocket + REST
┌──────────────────────────▼───────────────────────────────┐
│                    Agent Runtime (Backend)                │
│           Spring Boot 3.3 + Clean Architecture            │
│                                                           │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │ AI Tools     │  │ Browser Auto │  │ Email Monitor   │ │
│  │ Score        │  │ Playwright   │  │ IMAP/Parsing    │ │
│  │ Tailor       │  │ Portal Detect│  │ Signal Detect   │ │
│  │ Cover Letter │  │ Form Fill    │  │ Notifications   │ │
│  │ Interview    │  │ Stealth      │  │                 │ │
│  │ ATS Analyze  │  │ Proxy Rotate │  │                 │ │
│  │ Company Intel│  │ Cookie Store │  │                 │ │
│  └──────┬──────┘  └──────┬───────┘  └────────┬────────┘ │
│         │                │                    │           │
│  ┌──────▼────────────────▼────────────────────▼────────┐ │
│  │              Domain Layer (DDD)                      │ │
│  │  Mission | Task | Memory | Job | CandidateProfile    │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────┬───────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
   ┌────────────┐  ┌────────────┐  ┌────────────┐
   │ PostgreSQL │  │   Redis    │  │   Ollama   │
   │  Job Data  │  │   Cache    │  │  Local AI  │
   │  Profiles  │  │   Queue    │  │  Llama 3   │
   │  Memory    │  │   Sessions │  │            │
   └────────────┘  └────────────┘  └────────────┘
```

## Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.3** (Clean Architecture / DDD / Hexagonal)
- **PostgreSQL 16** with Flyway migrations
- **Redis 7** for caching and task queues
- **Ollama** (default local AI) with Llama 3 / Nomic Embed
- **Playwright Java** for browser automation
- **SpringDoc OpenAPI** for API documentation
- **Micrometer + Prometheus** for metrics
- **OpenTelemetry** for distributed tracing

### Frontend
- **Next.js 14** (App Router) + **TypeScript**
- **Tailwind CSS** + **Radix UI**
- **React Query** for data fetching
- **Zustand** for state management
- **WebSocket** for real-time agent updates

### Infrastructure
- **Docker** multi-stage builds (backend + frontend)
- **Docker Compose** (dev: full stack, prod: minimal)
- **GitHub Actions** (CI/CD: build, test, security, release)
- **Prometheus + Grafana** monitoring dashboards
- **Loki + Tempo** for logs and traces

## Quick Start

### Prerequisites
- **Java 21** ([OpenJDK](https://openjdk.org/projects/jdk/21/))
- **Node.js 20+** ([nodejs.org](https://nodejs.org/))
- **PostgreSQL 16** ([postgresql.org](https://www.postgresql.org/))
- **Ollama** ([ollama.com](https://ollama.com/)) — for local AI

### 1. Clone & Setup

```bash
git clone https://github.com/manojkushwah91/JobPilotAi.git
cd JobPilotAi
```

### 2. Install Ollama & Pull Model

```bash
# Install Ollama (macOS/Linux)
curl -fsSL https://ollama.com/install.sh | sh

# Pull the AI model
ollama pull llama3.2
ollama pull nomic-embed-text
```

### 3. Setup Database

```bash
# Create PostgreSQL database
psql -U postgres -c "CREATE USER jobpilot WITH PASSWORD 'jobpilot_dev';"
psql -U postgres -c "CREATE DATABASE jobpilot OWNER jobpilot;"
```

### 4. Start Backend

```bash
cd backend
./mvnw spring-boot:run
# Backend starts at http://localhost:8080
```

### 5. Start Frontend

```bash
cd frontend
npm install
npm run dev
# Frontend starts at http://localhost:3000
```

### 6. Open Mission Control

Navigate to **http://localhost:3000** and:
1. Sign up with your email
2. Upload your resume (auto-parsed)
3. Fill your profile (skills, experience, preferences)
4. Create a Mission (target role, salary, location)
5. Start the Agent — it applies while you supervise

### Docker (All-in-One)

```bash
docker compose up -d
# PostgreSQL + Redis + Backend + Frontend
```

## Project Structure

```
JobPilotAi/
├── backend/                          # Spring Boot backend
│   ├── jobpilot-common/              # Shared utilities
│   ├── jobpilot-domain/              # Domain entities (DDD)
│   ├── jobpilot-application/         # Application services + AI tools
│   │   └── agent/tools/              # 10 AI tools (scoring, tailoring, etc.)
│   ├── jobpilot-infrastructure/      # Adapters (persistence, browser, email)
│   │   └── automation/               # Playwright, portal detection, stealth
│   ├── jobpilot-interfaces/          # REST controllers, WebSocket
│   └── jobpilot-bootstrap/           # Main entry + config
├── frontend/                         # Next.js 14 frontend
│   └── src/app/(dashboard)/          # Dashboard, Jobs, Applications, Settings
├── infrastructure/                   # Docker, monitoring configs
├── .github/workflows/               # CI/CD (build, test, security, release)
├── docker-compose.yml               # Production compose
└── monitoring/                       # Prometheus + Grafana dashboards
```

## AI Tools

| Tool | Name | Description |
|------|------|-------------|
| `DISCOVER_JOBS` | Job Discovery | Searches database for matching jobs |
| `SCRAPE_JOBS` | Multi-Board Scraper | Scrapes Indeed/LinkedIn with descriptions |
| `RANK_JOB` | Job Scoring | Scores job match 0-100 with explanations |
| `TAILOR_RESUME` | Resume Tailoring | Rewrites resume to match job keywords |
| `GENERATE_COVER_LETTER` | Cover Letter | Generates personalized cover letter |
| `ANALYZE_RESUME` | ATS Analyzer | Checks ATS compatibility + improvements |
| `PREPARE_INTERVIEW` | Interview Prep | Generates questions, answers, strategy |
| `RESEARCH_COMPANY` | Company Intel | Researches company culture, finances, news |
| `SUBMIT_APPLICATION` | Auto-Apply | Fills forms via browser automation |
| `MONITOR_EMAILS` | Email Monitor | Detects interviews, rejections, offers |

## API Documentation

Once the backend is running, access Swagger UI at:
- **http://localhost:8080/swagger-ui.html**

## Monitoring

```bash
# Start monitoring stack
cd monitoring
docker compose -f docker-compose.monitoring.yml up -d

# Grafana: http://localhost:3001 (admin/admin)
# Prometheus: http://localhost:9090
```

## Testing

```bash
# Backend (332+ tests)
cd backend
mvn test

# Frontend
cd frontend
npm test
```

## Documentation

| Document | Description |
|----------|-------------|
| [C4 Architecture](C4_ARCHITECTURE.md) | System architecture diagrams (Levels 1-4) |
| [High-Level Design](HLD.md) | System design overview |
| [Low-Level Design](LLD.md) | Detailed component design |
| [Database Design](DATABASE_DESIGN.md) | PostgreSQL schema |
| [Security](SECURITY.md) | Authentication, authorization, encryption |
| [Browser Automation](BROWSER_AUTOMATION.md) | Playwright automation architecture |
| [AI Provider Layer](AI_PROVIDER_LAYER.md) | Ollama + cloud AI integration |
| [Logging & Observability](LOGGING_OBSERVABILITY.md) | Monitoring stack |

## How It Works

```
1. SCRAPE    → Playwright scrapes Indeed/LinkedIn for jobs with descriptions
2. DISCOVER  → Query database for jobs matching your criteria
3. SCORE     → AI scores each job 0-100 (skills, experience, salary fit)
4. TAILOR    → AI rewrites your resume to match the job description
5. COVER     → AI writes a personalized cover letter
6. APPLY     → Browser opens job page, detects portal, fills form
7. MONITOR   → Scans email for interview invites, rejections, offers
8. LEARN     → Agent remembers what worked and improves over time
```

## Privacy

- **Offline-First:** AI runs locally via Ollama — no data sent to cloud
- **Data Stays Local:** All profiles, resumes, and history on your machine
- **Cloud AI is Opt-In:** OpenAI/Gemini/Claude available but disabled by default
- **Open Source:** Full codebase on GitHub — verify everything

## License

MIT — see [LICENSE](LICENSE)

---

**JobPilot AI — The AI applies. You supervise.**
