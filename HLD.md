# JobPilot AI v2.0 — High Level Design (HLD)

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Architecture Overview
2. Agent Runtime Architecture
3. Module Diagram & Bounded Contexts
4. Request Flow
5. Deployment Diagram
6. Technology Decisions & Rationale
7. Communication Patterns
8. Data Flow Architecture
9. Integration Points
10. Security Architecture (High Level)
11. Observability Architecture (High Level)
12. Scaling Boundaries
13. Appendix: C4 Context

---

## 1. Architecture Overview

### 1.1 Architectural Philosophy

JobPilot AI v2.0 follows **Agent-Centric Architecture**. The entire system revolves around the Agent Runtime—the autonomous intelligence that executes the job hunting workflow. The web application is merely a control center for supervising the agent.

The architecture combines:
- **Clean Architecture** (Robert C. Martin) for maintainability
- **Domain-Driven Design** for bounded contexts
- **Modular Monolith** for simplicity (can decompose to microservices later)
- **Agent-Oriented Design** for autonomous execution

### 1.2 Core Principles

| Principle | Application |
|-----------|-------------|
| **Agent-Centric** | All functionality flows through the Agent Runtime |
| **Offline-First** | Default AI provider is Ollama (local), cloud is optional |
| **Mission-Driven** | Users define Missions, agent executes autonomously |
| **Memory-Persistent** | Agent learns and remembers across sessions |
| **Tool-Based** | Agent capabilities are composable tools with clear interfaces |
| **Dependency Inversion** | Domain depends on nothing, Infrastructure depends on abstractions |
| **Strict Layering** | Interfaces → Application → Domain → Infrastructure |

### 1.3 High-Level System Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                │
│  ┌──────────────────┐  ┌──────────────────┐                        │
│  │  Next.js Web App │  │  WebSocket Client│                        │
│  │  Mission Control│  │  Real-time Updates│                        │
│  └────────┬─────────┘  └────────┬─────────┘                        │
└───────────┼─────────────────────┼────────────────────────────────────┘
            │                     │
            │ HTTPS/WSS (TLS 1.3)│
            │                     │
┌───────────▼─────────────────────▼────────────────────────────────────┐
│                      API LAYER                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Spring Boot REST Controllers                                 │   │
│  │  • Mission endpoints                                          │   │
│  │  • Agent control endpoints                                    │   │
│  │  • Candidate endpoints                                         │   │
│  │  • Application endpoints (read-only)                          │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  WebSocket Handlers                                            │   │
│  │  • Agent status updates                                        │   │
│  │  • Log streaming                                              │   │
│  │  • Notification push                                           │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────┬──────────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────────┐
│                   APPLICATION LAYER (MODULAR MONOLITH)              │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    AGENT RUNTIME (CORE)                        │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │   │
│  │  │ Agent Loop   │  │ Tool Layer   │  │ Memory Layer │       │   │
│  │  │ Observe      │  │ AI Tools     │  │ Long-term    │       │   │
│  │  │ Think        │  │ Browser Tools│  │ Short-term   │       │   │
│  │  │ Plan         │  │ Discovery    │  │ Knowledge    │       │   │
│  │  │ Execute      │  │ Storage      │  │ Episode      │       │   │
│  │  │ Verify       │  │              │  │              │       │   │
│  │  │ Learn        │  │              │  │              │       │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │  Mission │  │Candidate │  │  Job     │  │Application│           │
│  │  Service │  │ Service  │  │ Service  │  │ Service   │           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
│  ┌──────────┐  ┌──────────┐                                        │
│  │ Identity  │  │Notification│                                       │
│  │ Service  │  │ Service   │                                       │
│  └──────────┘  └──────────┘                                        │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  SHARED KERNEL                                                │   │
│  │  • Common domain primitives (Email, Money, etc.)             │   │
│  │  • Cross-cutting: Security, Auditing, Caching, Logging      │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────┬──────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│    Domain    │  │ AI Provider  │  │ Browser Auto │
│              │  │    Layer     │  │   Framework   │
│ Entities     │  │              │  │              │
│ Value Objects│  │ Ollama (def) │  │ Generic       │
│ Domain Events│  │ OpenAI (opt) │  │ Adapters     │
└──────────────┘  └──────────────┘  └──────────────┘
        │                 │                 │
        └─────────────────┼─────────────────┘
                          │
                          ▼
            ┌──────────────┐
            │Infrastructure│
            │              │
            │ PostgreSQL   │
            │ Redis        │
            │ File Storage │
            │ Security     │
            │ Config       │
            └──────────────┘
```

---

## 2. Agent Runtime Architecture

### 2.1 Agent Loop

The Agent Loop is the heart of the system. It continuously executes six phases:

```
┌──────────────────────────────────────────────────────────────┐
│                      AGENT LOOP                                 │
└──────────────────────────────────────────────────────────────┘

1. OBSERVE
   - Check Mission status
   - Check current task queue
   - Check memory (what happened last)
   - Check external state (new jobs, application responses)
   - Output: Current state snapshot

2. THINK
   - Use AI to reason about current state
   - Evaluate progress toward Mission
   - Identify obstacles
   - Generate hypotheses
   - Output: Reasoning result, next action recommendation

3. PLAN
   - Break down Mission into tasks
   - Prioritize tasks based on urgency and value
   - Estimate resources needed
   - Create execution plan
   - Output: Task queue with priorities

4. EXECUTE
   - Execute tasks using tools
   - Call AI tools for reasoning
   - Call browser tools for automation
   - Call discovery tools for job search
   - Call storage tools for persistence
   - Output: Task results, errors

5. VERIFY
   - Verify task completion
   - Check for errors
   - Validate results
   - Take screenshots for verification
   - Output: Verification result, confidence score

6. LEARN
   - Update memory with results
   - Update knowledge store with strategies
   - Refine future decisions
   - Create episode memory
   - Output: Updated memory

7. REPEAT
   - Return to OBSERVE
   - Continue until Mission complete or stopped
```

### 2.2 Tool Layer

The Tool Layer provides composable capabilities for the Agent Runtime:

```
┌──────────────────────────────────────────────────────────────┐
│                        TOOL LAYER                               │
└──────────────────────────────────────────────────────────────┘

AI TOOLS
├── ResumeParserTool: Extract skills, experience, education
├── JobAnalyzerTool: Analyze job description, compute compatibility
├── ResumeTailorTool: Tailor resume for specific job
├── CoverLetterTool: Generate job-specific cover letter
├── AnswerGeneratorTool: Generate answers for application questions
├── JobRankerTool: Rank jobs by compatibility score
├── ScamDetectorTool: Detect scam jobs
└── SkillGapTool: Identify skill gaps

BROWSER TOOLS
├── BrowserManagerTool: Manage Playwright browser instances
├── DOMAnalyzerTool: Analyze DOM structure, detect elements
├── PageClassifierTool: Classify page type (login, form, listing)
├── ActionPlannerTool: Plan action sequence based on page type
├── FormEngineTool: Fill form fields intelligently
├── UploadEngineTool: Upload files (resume, cover letter)
├── QuestionEngineTool: Answer application questions
├── ScreenshotTool: Capture screenshots
├── RetryEngineTool: Retry failed actions with exponential backoff
├── RecoveryEngineTool: Recover from errors
└── SessionManagerTool: Manage browser sessions and cookies

DISCOVERY TOOLS
├── JobDiscoveryTool: Search multiple job boards
└── JobDeduplicationTool: Remove duplicate job listings

STORAGE TOOLS
├── ResumeStorageTool: Store and retrieve resumes
├── JobStorageTool: Store and retrieve job listings
├── ApplicationStorageTool: Store application results
└── ScreenshotStorageTool: Store and retrieve screenshots
```

### 2.3 Memory Layer

The Memory Layer provides persistent memory for the Agent Runtime:

```
┌──────────────────────────────────────────────────────────────┐
│                      MEMORY LAYER                               │
└──────────────────────────────────────────────────────────────┘

LONG-TERM MEMORY (PostgreSQL)
├── User Preferences: "Never apply to TCS", "Prefer remote"
├── Outcomes: "Rejected by Microsoft", "Offer from Adobe"
├── Strategies: "LinkedIn Easy Apply works best on Tuesdays"
└── Knowledge: "Greenhouse ATS requires cover letter for senior roles"

SHORT-TERM MEMORY (Redis)
├── Current Context: "Currently applying to Adobe"
├── Recent Actions: "Just submitted application to Google"
├── Temporary State: "Waiting for CAPTCHA completion"
└── Session Data: "Current browser session cookies"

KNOWLEDGE STORE (PostgreSQL with pgvector)
├── Embeddings: Vector embeddings for semantic search
├── Patterns: Learned patterns from successful applications
└── Rules: Derived rules from outcomes

EPISODE MEMORY (PostgreSQL)
├── Complete application cycles with all steps
├── Success/failure analysis
└── Lessons learned
```

---

## 3. Module Diagram & Bounded Contexts

### 3.1 Bounded Contexts

```
┌──────────────────────────────────────────────────────────────┐
│                    BOUNDED CONTEXTS                            │
└──────────────────────────────────────────────────────────────┘

┌──────────────────┐
│  AGENT RUNTIME   │  ← Core context, owns the agent loop
│                  │
│  - Agent Loop    │
│  - Tools         │
│  - Memory        │
│  - Planning      │
│  - Reasoning     │
└──────────────────┘
         │
         │ Uses
         │
┌──────────────────┐
│   MISSION        │  ← User's job hunting goals
│                  │
│  - Mission       │
│  - MissionConfig │
│  - MissionMetrics│
└──────────────────┘
         │
         │ Uses
         │
┌──────────────────┐
│   CANDIDATE      │  ← User's professional profile
│                  │
│  - Profile       │
│  - Skills        │
│  - Experience    │
│  - Education     │
└──────────────────┘
         │
         │ Uses
         │
┌──────────────────┐
│      JOB         │  ← Job listings and analysis
│                  │
│  - JobListing    │
│  - JobAnalysis   │
│  - JobRanking    │
└──────────────────┘
         │
         │ Uses
         │
┌──────────────────┐
│  APPLICATION     │  ← Agent-submitted applications (read-only)
│                  │
│  - Application   │
│  - AutomationResult │
│  - Screenshot    │
└──────────────────┘
         │
         │ Uses
         │
┌──────────────────┐
│   IDENTITY       │  ← Authentication and authorization
│                  │
│  - User          │
│  - Auth          │
│  - Session       │
└──────────────────┘
         │
         │ Uses
         │
┌──────────────────┐
│  NOTIFICATION    │  ← Agent alerts to user
│                  │
│  - Notification  │
│  - Alert         │
│  - Push          │
└──────────────────┘
```

### 3.2 Module Dependencies

```
┌──────────────────────────────────────────────────────────────┐
│                    MODULE DEPENDENCIES                          │
└──────────────────────────────────────────────────────────────┘

jobpilot-interfaces (REST, WebSocket)
    ↓ depends on
jobpilot-application (Mission, Candidate, Job, Application, Identity, Notification)
    ↓ depends on
jobpilot-agent-runtime (Agent Loop, Tools, Memory)
    ↓ depends on
jobpilot-domain (Mission, Candidate, Job, Application, Memory, Task, AgentState)
    ↓ depends on
jobpilot-common (Shared primitives)

jobpilot-interfaces
    ↓ depends on
jobpilot-application

jobpilot-application
    ↓ depends on
jobpilot-ai-provider (Ollama, OpenAI, Gemini, Claude)
jobpilot-browser-automation (Generic framework, Adapters)

jobpilot-ai-provider
    ↓ depends on
jobpilot-domain

jobpilot-browser-automation
    ↓ depends on
jobpilot-domain

jobpilot-infrastructure
    ↓ depends on
jobpilot-domain
    ↓ implements ports from
jobpilot-application
```

---

## 4. Request Flow

### 4.1 Mission Creation Flow

```
User → POST /api/v1/missions
  ↓
MissionController (interfaces)
  ↓
CreateMissionUseCase (application)
  ↓
MissionService (application)
  ↓
MissionRepository (infrastructure)
  ↓
PostgreSQL (infrastructure)
  ↓
Mission created, return MissionResponse
```

### 4.2 Agent Start Flow

```
User → POST /api/v1/missions/{id}/start
  ↓
MissionController (interfaces)
  ↓
StartMissionUseCase (application)
  ↓
AgentRuntime (agent-runtime)
  ↓
AgentLoop.start()
  ↓
Observe Phase → Think Phase → Plan Phase → Execute Phase
  ↓
WebSocket push: Agent status updated to RUNNING
```

### 4.3 Job Discovery Flow (Agent Execution)

```
AgentLoop (Execute Phase)
  ↓
JobDiscoveryTool (agent-runtime)
  ↓
JobDiscoveryService (application)
  ↓
JobBoardAdapters (browser-automation)
  ↓
Playwright (browser-automation)
  ↓
Job Boards (External: LinkedIn, Indeed, etc.)
  ↓
Job listings returned
  ↓
JobDeduplicationTool (agent-runtime)
  ↓
JobStorageTool (agent-runtime)
  ↓
JobRepository (infrastructure)
  ↓
PostgreSQL (infrastructure)
```

### 4.4 Job Analysis Flow (AI Execution)

```
AgentLoop (Execute Phase)
  ↓
JobAnalyzerTool (agent-runtime)
  ↓
AiProvider (ai-provider)
  ↓
OllamaProvider (ai-provider)
  ↓
Ollama (Local: http://localhost:11434)
  ↓
AI response: compatibility score, matched skills, missing skills
  ↓
JobAnalysis created
  ↓
JobRepository (infrastructure)
  ↓
PostgreSQL (infrastructure)
```

### 4.5 Application Submission Flow (Browser Automation)

```
AgentLoop (Execute Phase)
  ↓
BrowserManagerTool (agent-runtime)
  ↓
SiteAdapter (browser-automation)
  ↓
FormEngineTool (agent-runtime)
  ↓
UploadEngineTool (agent-runtime)
  ↓
Playwright (browser-automation)
  ↓
Job Board Application Page (External)
  ↓
ScreenshotTool (agent-runtime)
  ↓
ApplicationStorageTool (agent-runtime)
  ↓
ApplicationRepository (infrastructure)
  ↓
PostgreSQL (infrastructure)
  ↓
WebSocket push: Application submitted
```

---

## 5. Deployment Diagram

### 5.1 Development Deployment

```
┌──────────────────────────────────────────────────────────────┐
│                  DEVELOPMENT DEPLOYMENT                        │
└──────────────────────────────────────────────────────────────┘

Developer Machine
  ├── Docker Desktop
  │   ├── jobpilot-api (Spring Boot)
  │   ├── postgres (PostgreSQL 16 + pgvector)
  │   └── redis (Redis 7)
  ├── Ollama (Local AI)
  │   └── Models: Llama 3.x, Qwen 2.5, Mistral
  ├── Node.js (Frontend dev server)
  │   └── Next.js dev server (port 3000)
  └── Browser (Chrome)
      └── http://localhost:3000
```

### 5.2 Production Deployment (Single-User)

```
┌──────────────────────────────────────────────────────────────┐
│                PRODUCTION DEPLOYMENT (Single-User)              │
└──────────────────────────────────────────────────────────────┘

User Server / Powerful Workstation
  ├── Docker Compose
  │   ├── jobpilot-api (Spring Boot)
  │   ├── postgres (PostgreSQL 16 + pgvector)
  │   ├── redis (Redis 7)
  │   └── nginx (Reverse proxy)
  ├── Ollama (Local AI)
  │   └── Models: Llama 3.x, Qwen 2.5, Mistral
  └── File Storage
      └── /var/lib/jobpilot/uploads

Network
  ├── Internet (for job board scraping)
  └── No cloud AI (offline-first)
```

### 5.3 Production Deployment (Multi-User - Future)

```
┌──────────────────────────────────────────────────────────────┐
│               PRODUCTION DEPLOYMENT (Multi-User)               │
└──────────────────────────────────────────────────────────────┘

Kubernetes Cluster
  ├── Deployment: jobpilot-api (3 replicas)
  ├── Service: ClusterIP (port 8080)
  ├── Ingress: ALB + SSL
  ├── ConfigMap: application-prod.yml
  └── Secret: DB creds, JWT keys

External Services
  ├── PostgreSQL (Managed: RDS)
  ├── Redis (Managed: ElastiCache)
  ├── Ollama (Dedicated server per user or shared with isolation)
  └── S3 (File storage)

Monitoring
  ├── Prometheus
  ├── Grafana
  └── Jaeger
```

---

## 6. Technology Decisions & Rationale

### 6.1 Backend Technology Stack

| Technology | Version | Rationale |
|------------|---------|-----------|
| Java | 21 | Latest LTS, performance improvements, virtual threads |
| Spring Boot | 3.3.5 | Mature framework, extensive ecosystem, rapid development |
| PostgreSQL | 16 | Robust relational DB, pgvector for embeddings, JSONB for flexibility |
| Redis | 7 | Fast in-memory cache, pub/sub for real-time |
| Ollama | Latest | Local LLM inference, offline-first, privacy |
| Playwright Java | Latest | Reliable browser automation, cross-browser support |
| Maven | Latest | Standard Java build tool, dependency management |

### 6.2 Frontend Technology Stack

| Technology | Version | Rationale |
|------------|---------|-----------|
| Next.js | 14 | React framework with SSR, App Router, excellent DX |
| TypeScript | Latest | Type safety, better developer experience |
| Tailwind CSS | Latest | Utility-first CSS, rapid UI development |
| Radix UI | Latest | Accessible UI components, unstyled |
| Zustand | Latest | Lightweight state management |
| React Query | Latest | Data fetching, caching, synchronization |
| WebSocket | Native | Real-time communication |

### 6.3 AI Technology Decisions

**Decision: Ollama as Default AI Provider**

**Rationale:**
- Offline-first operation (no internet required for AI)
- Privacy (data never leaves user's machine)
- Cost (no API costs)
- Control (user chooses models)
- Open-source (transparent, no vendor lock-in)

**Trade-offs:**
- Requires hardware resources (RAM, CPU)
- Slower inference than cloud AI
- Limited model selection compared to cloud

**Mitigation:**
- Document hardware requirements
- Provide cloud AI fallback (optional)
- Optimize prompts for smaller models

### 6.4 Browser Automation Technology Decisions

**Decision: Playwright Java**

**Rationale:**
- Reliable browser automation
- Cross-browser support (Chromium, Firefox, WebKit)
- Headless mode for server execution
- Excellent API design
- Active development

**Trade-offs:**
- Resource-intensive (browser instances)
- Job boards may block automation

**Mitigation:**
- Implement human-like delays
- Use residential proxies (optional)
- Implement CAPTCHA handling

---

## 7. Communication Patterns

### 7.1 Synchronous Communication (REST)

```
Client → HTTP Request → Controller → Service → Repository → DB
Client ← HTTP Response ← Controller ← Service ← Repository ← DB
```

**Use Cases:**
- Mission CRUD operations
- Candidate profile management
- Application tracking (read-only)
- Authentication

### 7.2 Asynchronous Communication (WebSocket)

```
Client ← WebSocket Message ← Agent Runtime ← Event
```

**Use Cases:**
- Agent status updates
- Real-time log streaming
- Notification push
- Task completion events

### 7.3 Internal Communication (Method Calls)

```
Agent Loop → Tool → Service → Repository → DB
Agent Loop ← Result ← Tool ← Service ← Repository ← DB
```

**Use Cases:**
- Agent execution
- Tool invocation
- Memory operations

### 7.4 External Communication (HTTP/Scraping)

```
Agent → Browser Automation → Job Board (External)
Agent ← Job Data ← Browser Automation ← Job Board (External)
```

**Use Cases:**
- Job discovery
- Application submission
- Company research

---

## 8. Data Flow Architecture

### 8.1 Agent Execution Data Flow

```
┌──────────────────────────────────────────────────────────────┐
│                  AGENT EXECUTION DATA FLOW                      │
└──────────────────────────────────────────────────────────────┘

1. OBSERVE
   Mission (DB) → Current State
   Task Queue (Redis) → Pending Tasks
   Memory (PostgreSQL/Redis) → Context
   External State → New Jobs, Responses

2. THINK
   Current State → AI Provider (Ollama) → Reasoning Result

3. PLAN
   Reasoning Result → Task Planner → Task Queue (Redis)

4. EXECUTE
   Task Queue (Redis) → Tool Execution
   AI Tools → AI Provider (Ollama) → AI Response
   Browser Tools → Playwright → Job Board → Result
   Discovery Tools → Job Boards → Job Listings
   Storage Tools → Repository → DB

5. VERIFY
   Tool Result → Verification Logic → Confidence Score

6. LEARN
   Tool Result + Confidence → Memory Update
   Memory Update → PostgreSQL (Long-term)
   Memory Update → Redis (Short-term)
```

### 8.2 Mission Creation Data Flow

```
User Input → MissionRequest
  ↓
MissionController → CreateMissionUseCase
  ↓
MissionService → Mission.create()
  ↓
MissionRepository → save(Mission)
  ↓
PostgreSQL → INSERT INTO missions
  ↓
MissionResponse ← Mission
  ↓
User ← MissionResponse
```

### 8.3 Job Discovery Data Flow

```
Mission Criteria → JobDiscoveryTool
  ↓
JobDiscoveryService → JobBoardAdapters
  ↓
LinkedInAdapter → Playwright → LinkedIn → Job Listings
IndeedAdapter → Playwright → Indeed → Job Listings
  ↓
Job Listings → JobDeduplicationTool
  ↓
Deduplicated Jobs → JobStorageTool
  ↓
JobRepository → saveAll(JobListings)
  ↓
PostgreSQL → INSERT INTO job_listings
```

### 8.4 Application Submission Data Flow

```
Job + Tailored Resume + Cover Letter → BrowserManagerTool
  ↓
SiteAdapter → Playwright → Job Board Application Page
  ↓
FormEngineTool → Fill Form
  ↓
UploadEngineTool → Upload Resume, Cover Letter
  ↓
QuestionEngineTool → Answer Questions
  ↓
Submit → Job Board
  ↓
ScreenshotTool → Capture Screenshot
  ↓
ApplicationStorageTool → save(Application, Screenshot)
  ↓
ApplicationRepository → save(Application)
  ↓
PostgreSQL → INSERT INTO applications
  ↓
WebSocket Push → Notification to User
```

---

## 9. Integration Points

### 9.1 Ollama Integration

**Endpoint:** `http://localhost:11434`

**APIs Used:**
- `POST /api/generate` - Text generation
- `POST /api/embeddings` - Embedding generation
- `GET /api/tags` - List available models

**Auto-Detection:**
- On startup, check if Ollama is running
- If not running, guide user through installation
- If running, verify required models are available
- Download models if needed

### 9.2 Job Board Integration

**LinkedIn:**
- Method: Playwright scraping (Easy Apply)
- Rate Limit: 30 requests/minute
- Authentication: User credentials per session

**Indeed:**
- Method: Playwright scraping
- Rate Limit: 20 requests/minute
- Authentication: Not required for search

**Greenhouse:**
- Method: Playwright scraping
- Rate Limit: 10 requests/minute
- Authentication: Not required for search

**Lever:**
- Method: Playwright scraping
- Rate Limit: 10 requests/minute
- Authentication: Not required for search

**Workday:**
- Method: Playwright scraping
- Rate Limit: 5 requests/minute
- Authentication: Not required for search

### 9.3 Cloud AI Integration (Optional)

**OpenAI:**
- Endpoint: `https://api.openai.com/v1`
- API Key: User-provided (stored encrypted)
- Models: GPT-4, GPT-3.5-turbo

**Gemini:**
- Endpoint: `https://generativelanguage.googleapis.com/v1`
- API Key: User-provided (stored encrypted)
- Models: Gemini Pro

**Claude:**
- Endpoint: `https://api.anthropic.com/v1`
- API Key: User-provided (stored encrypted)
- Models: Claude 3 Opus, Sonnet, Haiku

---

## 10. Security Architecture (High Level)

### 10.1 Authentication

- JWT-based authentication with refresh token rotation
- Password hashing with BCrypt
- Email verification for registration
- Password reset via email token

### 10.2 Authorization

- Role-based access control (USER, ADMIN)
- Method-level security with @PreAuthorize
- Resource-level security (users can only access their own data)

### 10.3 Data Privacy

- All user data encrypted at rest (AES-256)
- AI inference runs locally (Ollama) by default
- Cloud AI opt-in only
- No data sharing with third parties without consent

### 10.4 Rate Limiting

- API rate limiting per user (100 requests/minute)
- Job board scraping rate limiting per source
- Browser automation rate limiting per domain

### 10.5 Input Validation

- All user input validated
- SQL injection prevention (JPA parameterized queries)
- XSS prevention (React escaping)
- CSRF protection (Spring Security)

---

## 11. Observability Architecture (High Level)

### 11.1 Metrics (Prometheus)

**Agent Metrics:**
- `agent_loop_duration_seconds` - Agent loop execution time
- `agent_task_success_total` - Successful task completions
- `agent_task_failure_total` - Failed task completions
- `agent_memory_size_bytes` - Memory size

**AI Metrics:**
- `ai_inference_duration_seconds` - AI inference time
- `ai_inference_tokens_total` - Token usage
- `ai_cache_hit_ratio` - Cache hit ratio

**Browser Metrics:**
- `browser_automation_success_total` - Successful automations
- `browser_automation_failure_total` - Failed automations
- `browser_captcha_detected_total` - CAPTCHA detections

**Business Metrics:**
- `jobs_found_total` - Jobs discovered
- `applications_submitted_total` - Applications submitted
- `interviews_scheduled_total` - Interviews scheduled

### 11.2 Logging (ELK Stack)

- Structured JSON logging (Logback)
- MDC fields: traceId, userId, agentId, taskId
- Log levels: DEBUG, INFO, WARN, ERROR
- Centralized logging in Elasticsearch

### 11.3 Tracing (OpenTelemetry)

- W3C Trace Context propagation
- Auto-instrumentation: Spring Boot, JDBC, HTTP, Redis
- Manual instrumentation: Agent Loop, Tools
- Trace visualization in Jaeger

---

## 12. Scaling Boundaries

### 12.1 Single-User Deployment (Default)

**Constraints:**
- One Agent Runtime instance per user
- Vertical scaling: more CPU, more RAM
- Browser automation limited by job board rate limits

**Scaling Strategy:**
- Increase CPU cores for parallel tool execution
- Increase RAM for larger AI models
- Optimize prompts for faster inference

### 12.2 Multi-User Deployment (Future)

**Constraints:**
- Horizontal scaling of API servers
- Each user has isolated agent instance
- Shared PostgreSQL and Redis
- Per-user rate limiting

**Scaling Strategy:**
- Kubernetes HPA for API servers
- Database connection pooling
- Redis clustering for cache
- Per-user resource quotas

---

## 13. Appendix: C4 Context

### 13.1 C4 Level 1 — System Context

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              EXTERNAL SYSTEMS                               │
│                                                                              │
│  ┌─────────────────────────────┐    ┌─────────────────────────────┐        │
│  │     [Person] Job Seeker     │    │     [Person] Admin          │        │
│  │  User who creates Missions, │    │  System administrator who │        │
│  │  supervises agent, monitors │    │  manages users, config,    │        │
│  │  progress, and receives     │    │  and monitors health.      │        │
│  │  notifications.             │    │                             │        │
│  └──────────────┬──────────────┘    └──────────────┬──────────────┘        │
│                 │                                  │                        │
│                 │ Uses [HTTPS/WSS]                  │ Uses [HTTPS]           │
│                 ▼                                  ▼                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   JobPilot AI v2.0 [Software System]                │   │
│  │  "Offline-First Autonomous AI Job Agent"                          │   │
│  │  Agent Runtime that autonomously searches, analyzes, tailors,     │   │
│  │  and applies to jobs while user supervises via Mission Control.    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                 │                                  │                        │
│                 │ Uses [HTTPS REST]                │                        │
│                 ▼                                  │                        │
│  ┌─────────────────────────────┐    ┌──────────────┴──────────────┐        │
│  │  Ollama [Ext System]        │    │  Job Boards [Ext System]    │        │
│  │  Local LLM inference        │    │  LinkedIn, Indeed, Greenhouse│        │
│  │  Models: Llama, Qwen, etc. │    │  Lever, Workday, Company Sites│       │
│  │  Default AI provider        │    │  Provides job listings      │        │
│  └─────────────────────────────┘    └─────────────────────────────┘        │
│                                                                              │
│  ┌─────────────────────────────┐                                           │
│  │  Cloud AI [Ext System]      │                                           │
│  │  OpenAI, Gemini, Claude     │                                           │
│  │  Optional cloud AI providers│                                           │
│  │  (Opt-in only)              │                                           │
│  └─────────────────────────────┘                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 13.2 C4 Level 2 — Container Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          JobPilot AI v2.0                                  │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    Mission Control (Web Application)                       │
│  Next.js 14 + TypeScript + Tailwind CSS + Radix UI                        │
│  • Agent status display                                                    │
│  • Current task display                                                    │
│  • Progress panel                                                          │
│  • Timeline                                                                │
│  • Log console                                                             │
│  • Control buttons (START/PAUSE/STOP)                                      │
│  • Chat interface                                                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │ HTTPS/WSS
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    API Application (Spring Boot)                            │
│  Java 21 + Spring Boot 3.3.5 + Clean Architecture                          │
│  • REST Controllers (Mission, Agent, Candidate, Application)               │
│  • WebSocket Handlers (Agent status, logs, notifications)                  │
│  • Application Services (Mission, Candidate, Job, Application)              │
│  • Agent Runtime (Agent Loop, Tools, Memory)                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   PostgreSQL         │  │      Redis            │  │   File Storage       │
│   (pgvector)         │  │                      │  │                      │
│   • Missions         │  │ • Task Queue         │  │ • Resumes            │
│   • Candidates       │  │ • Short-term Memory  │  │ • Cover Letters      │
│   • Jobs             │  │ • Cache              │  │ • Screenshots        │
│   • Applications     │  │                      │  │                      │
│   • Memory           │  │                      │  │                      │
│   • Tasks            │  │                      │  │                      │
│   • Agent States     │  │                      │  │                      │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Ollama (Local AI)                                        │
│  • Llama 3.x                                                              │
│  • Qwen 2.5                                                               │
│  • Mistral                                                                │
│  • DeepSeek                                                               │
│  • Gemma                                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Job Boards (External)                                    │
│  • LinkedIn                                                               │
│  • Indeed                                                                 │
│  • Greenhouse                                                             │
│  • Lever                                                                  │
│  • Workday                                                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

**End of HLD v2.0**
