# JobPilot AI v2.0 🤖

> Offline-First Autonomous AI Job Agent — The AI works 24/7 to get you interviews while you supervise.

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)]()
[![Next.js](https://img.shields.io/badge/Next.js-14-black)]()
[![Ollama](https://img.shields.io/badge/Ollama-Default-blue)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

## ✨ What is JobPilot AI?

JobPilot AI is **not** a job portal. It is **not** LinkedIn. It is **not** Indeed. It is **not** another CRUD dashboard.

**JobPilot AI is an offline-first autonomous AI job agent.**

You provide your resume and preferences once. The AI continuously searches for suitable jobs, analyzes every opportunity, tailors resumes, generates cover letters, fills application forms, and submits applications—all while you monitor progress through Mission Control.

**The AI executes. You supervise.**

## 🎯 Core Philosophy

- **Agent-Centric:** Everything revolves around the Agent Runtime
- **Offline-First:** Default AI provider is Ollama (local). Cloud AI is optional.
- **Mission-Driven:** You define Missions, the agent executes autonomously.
- **Memory-Persistent:** The agent learns and remembers across sessions.
- **Privacy-First:** Your data stays on your machine by default.

## 🚀 Key Features

| Feature | Description |
|---------|-------------|
| **🤖 Agent Runtime** | Autonomous agent that executes Observe-Think-Plan-Execute-Verify-Learn loop |
| **🎯 Mission Control** | Define job hunting goals (salary, locations, companies, limits) |
| **🧠 AI Tools** | Resume parsing, job analysis, resume tailoring, cover letter generation (Ollama) |
| **🌐 Browser Automation** | Generic automation framework with site adapters (LinkedIn, Indeed, etc.) |
| **💾 Long-Term Memory** | Agent remembers preferences, outcomes, strategies across sessions |
| **💬 Chat Interface** | Natural language control: "Find remote Java jobs", "Pause until tomorrow" |
| **📊 Real-Time Monitoring** | Agent status, current task, progress, timeline, logs |
| **🔐 Privacy-First** | Local AI inference (Ollama), data never leaves your machine by default |

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│              Mission Control (Frontend)          │
│         Next.js 14 + TypeScript + Tailwind       │
│  Agent Status | Current Task | Progress | Chat   │
└────────────────┬────────────────────────────────┘
                 │ WebSocket
┌────────────────▼────────────────────────────────┐
│              Agent Runtime (Backend)             │
│  Agent Loop | Tools | Memory | Planning | Reasoning│
└────────────────┬────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
┌──────────────┐  ┌──────────────┐
│  Ollama (AI)  │  │ Browser Auto │
│  Local LLM    │  │ Playwright   │
└──────────────┘  └──────────────┘
        │                 │
        └────────┬────────┘
                 ▼
┌────────────────────────────────┐
│  PostgreSQL | Redis | Storage  │
└────────────────────────────────┘
```

## 🛠 Tech Stack

### Backend
- **Java 21** with **Spring Boot 3.3.5**
- **Clean Architecture** (Hexagonal, Domain-Driven Design)
- **PostgreSQL 16** with pgvector (embeddings)
- **Redis 7** (task queue, short-term memory, cache)
- **Ollama** (default local AI provider)
- **Playwright Java** (browser automation)
- **JWT** with refresh token rotation

### Frontend
- **Next.js 14** (App Router)
- **TypeScript** with strict mode
- **Tailwind CSS** for styling
- **Radix UI** for accessible primitives
- **React Query** for data fetching
- **Zustand** for state management
- **WebSocket** for real-time updates

## 🚀 Quick Start

### Prerequisites
- **Docker** 24.0+
- **Docker Compose** 2.20+
- **Ollama** 0.1.0+ (for local AI)

### 1. Clone & Setup
```bash
git clone https://github.com/manojkushwah91/JobPilotAi.git
cd JobPilotAi
```

### 2. Install Ollama
```bash
# Linux
curl -fsSL https://ollama.com/install.sh | sh

# macOS
brew install ollama

# Windows
winget install ollama
```

### 3. Pull AI Model
```bash
ollama pull llama3
```

### 4. Start Services
```bash
docker compose up -d
```

This starts:
- PostgreSQL (with pgvector)
- Redis
- Backend API (Spring Boot)
- Frontend (Next.js)

### 5. Open Browser
- **Mission Control:** http://localhost:3000
- **Backend API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html

## 📦 Project Structure

```
JobPilotAi/
├── backend/
│   ├── jobpilot-common/              # Shared utilities
│   ├── jobpilot-domain/              # Domain entities (Mission, Candidate, Job, Memory, Task, AgentState)
│   ├── jobpilot-agent-runtime/       # Agent Runtime (CORE - NEW)
│   │   ├── loop/                    # Agent Loop phases
│   │   ├── tools/                   # AI tools, Browser tools, Discovery tools, Storage tools
│   │   ├── memory/                  # Long-term, Short-term, Knowledge, Episode memory
│   │   ├── planning/                # Planner, Task planner, Workflow engine
│   │   └── reasoning/               # Reasoner, Decision engine
│   ├── jobpilot-ai-provider/        # AI Provider Layer (NEW - Ollama-first)
│   │   ├── ollama/                  # Ollama provider (default)
│   │   ├── openai/                  # OpenAI provider (optional)
│   │   ├── gemini/                  # Gemini provider (optional)
│   │   └── claude/                  # Claude provider (optional)
│   ├── jobpilot-browser-automation/ # Browser Automation Framework (NEW)
│   │   ├── BrowserManager.java
│   │   ├── FormEngine.java
│   │   ├── UploadEngine.java
│   │   └── adapters/                # LinkedIn, Indeed, Greenhouse, Lever, Workday
│   ├── jobpilot-application/        # Application services
│   │   ├── mission/                 # Mission services (NEW)
│   │   ├── candidate/               # Candidate profile services (NEW)
│   │   ├── job/                     # Job services (refactored)
│   │   ├── application/             # Application services (read-only, refactored)
│   │   ├── identity/                # Authentication (KEEP)
│   │   └── notification/            # Notification services (KEEP)
│   ├── jobpilot-infrastructure/     # Persistence, external integrations
│   │   ├── persistence/             # JPA repositories
│   │   └── ai/                     # Ollama adapter (replaces FallbackAiProvider)
│   ├── jobpilot-interfaces/         # REST controllers, WebSocket handlers
│   │   ├── mission/                 # Mission endpoints (NEW)
│   │   ├── agent/                   # Agent control endpoints (NEW)
│   │   ├── candidate/               # Candidate endpoints (NEW)
│   │   ├── chat/                    # Chat endpoints (NEW)
│   │   ├── application/             # Application endpoints (read-only)
│   │   └── identity/                # Authentication endpoints (KEEP)
│   └── jobpilot-bootstrap/          # Main application entry point
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── mission-control/      # Mission Control (NEW - replaces dashboard)
│   │   │   │   ├── page.tsx        # Agent supervision interface
│   │   │   │   ├── chat/           # Chat interface
│   │   │   │   ├── missions/        # Mission management
│   │   │   │   └── candidate/       # Candidate profile
│   │   │   └── applications/        # Application tracking (read-only)
│   │   ├── components/
│   │   │   ├── mission-control/     # Agent status, progress, timeline, logs
│   │   │   ├── chat/                # Chat components
│   │   │   ├── missions/            # Mission cards, forms, progress
│   │   │   ├── candidate/           # Profile editor, skills editor
│   │   │   └── ui/                  # Reusable UI components (KEEP)
│   │   └── lib/
│   │       ├── api/                 # API endpoints
│   │       └── websocket/           # WebSocket client
├── docker-compose.yml              # Development services
└── docker-compose.prod.yml         # Production services
```

## 🧪 Testing

```bash
# Backend
cd backend && mvn test

# Frontend unit tests
cd frontend && npm test

# E2E tests
cd frontend && npm run e2e
```

## 📖 Documentation

- [SRS v2.0](SRS_V2.md) - Software Requirements Specification
- [HLD v2.0](HLD_V2.md) - High Level Design
- [LLD v2.0](LLD_V2.md) - Low Level Design
- [C4 Architecture v2.0](C4_ARCHITECTURE_V2.md) - C4 Architecture Diagrams
- [Database Design v2.0](DATABASE_DESIGN_V2.md) - Database Schema
- [Security v2.0](SECURITY_V2.md) - Security Documentation
- [Testing Strategy v2.0](TESTING_STRATEGY_V2.md) - Testing Strategy
- [Deployment v2.0](DEPLOYMENT_V2.md) - Deployment Guide

## 🎯 First Complete Journey

After approval, the first implementation will be:

1. **Upload Resume** → Parse Resume → Extract Profile
2. **Create Mission** → Set preferences (salary, locations, companies)
3. **Start Agent** → Agent searches jobs → Analyzes jobs → Ranks jobs
4. **Tailor Resume** → Generate Cover Letter → Launch Browser
5. **Fill Application** → Upload Resume → Pause if CAPTCHA
6. **User Approves** → Submit → Take Screenshot → Store Application
7. **Notify User** → Update Mission → Repeat

## 🔒 Privacy & Security

- **Offline-First:** Default AI inference is local (Ollama)
- **Data Encryption:** All user data encrypted at rest (AES-256)
- **No Cloud AI by Default:** Cloud AI providers are opt-in only
- **Open Source:** Fully open-source, transparent codebase

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

MIT — see [LICENSE](LICENSE).

## 🙏 Acknowledgments

- **Ollama** - Local LLM inference engine
- **Playwright** - Browser automation framework
- **Spring Boot** - Java application framework
- **Next.js** - React framework
- **PostgreSQL** - Relational database
- **Redis** - In-memory data store

---

**JobPilot AI v2.0 — The world's best open-source offline-first autonomous AI job agent.**
