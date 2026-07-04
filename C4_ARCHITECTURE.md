# JobPilot AI v2.0 — C4 Architecture

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. C4 Level 1 — System Context Diagram
2. C4 Level 2 — Container Diagram
3. C4 Level 3 — Component Diagram (per container)
4. C4 Level 4 — Code Diagram (key aggregates)

---

## 1. C4 Level 1 — System Context Diagram

**Scope:** JobPilot AI v2.0 Platform  
**Primary Audience:** Technical and non-technical stakeholders  

### 1.1 Diagram

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

### 1.2 Context Description

| Element | Type | Description |
|---------|------|-------------|
| **Job Seeker** | Person | Primary user who creates Missions, supervises agent, monitors progress, receives notifications |
| **Admin** | Person | Internal platform administrator who manages users, system config, monitors health |
| **JobPilot AI v2.0** | Software System | The platform — Agent Runtime that autonomously searches, analyzes, tailors, and applies to jobs |
| **Ollama** | External System | Local LLM inference engine (default AI provider) |
| **Cloud AI** | External System | Optional cloud AI providers (OpenAI, Gemini, Claude) |
| **Job Boards** | External System | Sources of job listings — LinkedIn, Indeed, Greenhouse, Lever, Workday, company career pages |

### 1.3 Relationships

| From | To | Description | Technology |
|------|----|-------------|------------|
| Job Seeker | JobPilot AI v2.0 | Creates Missions, supervises agent, views progress, receives notifications | HTTPS, WebSocket |
| Admin | JobPilot AI v2.0 | Manages users, config, views analytics | HTTPS |
| JobPilot AI v2.0 | Ollama | Sends prompts for AI reasoning (resume analysis, job matching, etc.) | HTTP REST |
| JobPilot AI v2.0 | Cloud AI | Sends prompts for AI reasoning (optional, opt-in) | HTTPS REST |
| JobPilot AI v2.0 | Job Boards | Scrapes job listings, submits applications | HTTPS (scraping) |

---

## 2. C4 Level 2 — Container Diagram

**Scope:** JobPilot AI v2.0 Single Application  
**Primary Audience:** Technical architects and developers  

### 2.1 Diagram

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
│  • Mission management                                                     │
│  • Candidate profile                                                      │
│  • Application tracking (read-only)                                        │
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
│  • AI Provider Layer (Ollama, OpenAI, Gemini, Claude)                      │
│  • Browser Automation Framework (Generic + Adapters)                      │
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

### 2.2 Container Description

| Container | Description | Technology |
|-----------|-------------|------------|
| **Mission Control** | Web application for agent supervision, mission management, candidate profile, application tracking | Next.js 14, TypeScript, Tailwind CSS, Radix UI |
| **API Application** | Backend API with Agent Runtime, AI Provider Layer, Browser Automation Framework | Java 21, Spring Boot 3.3.5 |
| **PostgreSQL** | Relational database with pgvector for embeddings | PostgreSQL 16 |
| **Redis** | In-memory cache and task queue | Redis 7 |
| **File Storage** | File storage for resumes, cover letters, screenshots | Local filesystem / S3 |
| **Ollama** | Local LLM inference engine | Ollama |
| **Job Boards** | External job board websites | LinkedIn, Indeed, Greenhouse, Lever, Workday |

### 2.3 Container Relationships

| From | To | Description | Technology |
|------|----|-------------|------------|
| Mission Control | API Application | REST API calls, WebSocket connections | HTTPS, WSS |
| API Application | PostgreSQL | Persist missions, candidates, jobs, applications, memory, tasks, agent states | JDBC |
| API Application | Redis | Task queue, short-term memory, cache | Lettuce |
| API Application | File Storage | Store resumes, cover letters, screenshots | Filesystem API |
| API Application | Ollama | AI inference for resume analysis, job matching, content generation | HTTP REST |
| API Application | Job Boards | Scrape job listings, submit applications | Playwright (HTTP) |

---

## 3. C4 Level 3 — Component Diagram

### 3.1 API Application Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        API Application                                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    Interfaces Layer                                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ MissionController│  │ AgentController  │  │ CandidateController│       │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ApplicationController│ │ WebSocketHandler │  │ AuthController   │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Application Layer                                       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ MissionService   │  │ AgentService     │  │ CandidateService │       │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ JobService       │  │ NotificationSvc  │  │ AuthService      │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Agent Runtime (CORE)                                    │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │   AgentLoop      │  │   Tool Layer     │  │   Memory Layer   │       │
│  │                  │  │                  │  │                  │       │
│  │ • Observe        │  │ • AI Tools       │  │ • Long-term      │       │
│  │ • Think          │  │ • Browser Tools  │  │ • Short-term     │       │
│  │ • Plan           │  │ • Discovery      │  │ • Knowledge      │       │
│  │ • Execute        │  │ • Storage        │  │ • Episode        │       │
│  │ • Verify         │  │                  │  │                  │       │
│  │ • Learn          │  │                  │  │                  │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
│  ┌──────────────────┐  ┌──────────────────┐                            │
│  │   Planning       │  │   Reasoning      │                            │
│  │   Layer          │  │   Layer          │                            │
│  └──────────────────┘  └──────────────────┘                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   AI Provider Layer  │  │ Browser Automation   │  │   Domain Layer       │
│                      │  │   Framework          │  │                      │
│  • OllamaProvider    │  │ • BrowserManager     │  │ • Mission            │
│  • OpenAIProvider    │  │ • SiteAdapters       │  │ • CandidateProfile    │
│  • GeminiProvider    │  │ • FormEngine         │  │ • JobListing         │
│  • ClaudeProvider    │  │ • UploadEngine       │  │ • Application        │
│                      │  │ • ScreenshotEngine   │  │ • Memory             │
│                      │  │ • RetryEngine        │  │ • Task               │
│                      │  │ • RecoveryEngine     │  │ • AgentState         │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                                    │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │ MissionRepo      │  │   Redis Client    │  │   File Storage    │       │
│  │ CandidateRepo    │  │                  │  │   Service         │       │
│  │ JobRepo          │  │                  │  │                  │       │
│  │ ApplicationRepo  │  │                  │  │                  │       │
│  │ MemoryRepo       │  │                  │  │                  │       │
│  │ TaskRepo         │  │                  │  │                  │       │
│  │ AgentStateRepo   │  │                  │  │                  │       │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘       │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Component Description

**Interfaces Layer:**
- `MissionController` - REST endpoints for mission CRUD and control
- `AgentController` - REST endpoints for agent control (start, pause, stop)
- `CandidateController` - REST endpoints for candidate profile management
- `ApplicationController` - REST endpoints for application tracking (read-only)
- `WebSocketHandler` - WebSocket handler for real-time agent updates
- `AuthController` - REST endpoints for authentication

**Application Layer:**
- `MissionService` - Mission business logic
- `AgentService` - Agent control business logic
- `CandidateService` - Candidate profile business logic
- `JobService` - Job discovery and analysis business logic
- `NotificationService` - Notification business logic
- `AuthService` - Authentication business logic

**Agent Runtime (CORE):**
- `AgentLoop` - Main loop orchestrator (Observe-Think-Plan-Execute-Verify-Learn)
- `Tool Layer` - AI tools, Browser tools, Discovery tools, Storage tools
- `Memory Layer` - Long-term memory, Short-term memory, Knowledge store, Episode memory
- `Planning Layer` - Planner, Task planner, Workflow engine
- `Reasoning Layer` - Reasoner, Decision engine

**AI Provider Layer:**
- `OllamaProvider` - Local Ollama provider (default)
- `OpenAIProvider` - OpenAI provider (optional)
- `GeminiProvider` - Gemini provider (optional)
- `ClaudeProvider` - Claude provider (optional)

**Browser Automation Framework:**
- `BrowserManager` - Playwright browser instance management
- `SiteAdapters` - LinkedIn, Indeed, Greenhouse, Lever, Workday adapters
- `FormEngine` - Form filling logic
- `UploadEngine` - File upload logic
- `ScreenshotEngine` - Screenshot capture logic
- `RetryEngine` - Retry logic with exponential backoff
- `RecoveryEngine` - Error recovery logic

**Domain Layer:**
- `Mission` - Mission entity
- `CandidateProfile` - Candidate profile entity
- `JobListing` - Job listing entity
- `Application` - Application entity
- `Memory` - Memory entity
- `Task` - Task entity
- `AgentState` - Agent state entity

**Infrastructure Layer:**
- `MissionRepo` - Mission JPA repository
- `CandidateRepo` - Candidate profile JPA repository
- `JobRepo` - Job JPA repository
- `ApplicationRepo` - Application JPA repository
- `MemoryRepo` - Memory JPA repository
- `TaskRepo` - Task JPA repository
- `AgentStateRepo` - Agent state JPA repository
- `Redis Client` - Redis client for task queue and cache
- `File Storage Service` - File storage service

---

## 4. C4 Level 4 — Code Diagram

### 4.1 Agent Runtime Code Diagram

```
com.jobpilot.agent/
├── AgentRuntime.java
├── loop/
│   ├── AgentLoop.java
│   ├── ObservePhase.java
│   ├── ThinkPhase.java
│   ├── PlanPhase.java
│   ├── ExecutePhase.java
│   ├── VerifyPhase.java
│   └── LearnPhase.java
├── tools/
│   ├── ai/
│   │   ├── ResumeParserTool.java
│   │   ├── JobAnalyzerTool.java
│   │   ├── ResumeTailorTool.java
│   │   ├── CoverLetterTool.java
│   │   ├── AnswerGeneratorTool.java
│   │   ├── JobRankerTool.java
│   │   ├── ScamDetectorTool.java
│   │   └── SkillGapTool.java
│   ├── browser/
│   │   ├── BrowserManagerTool.java
│   │   ├── DOMAnalyzerTool.java
│   │   ├── PageClassifierTool.java
│   │   ├── ActionPlannerTool.java
│   │   ├── FormEngineTool.java
│   │   ├── UploadEngineTool.java
│   │   ├── QuestionEngineTool.java
│   │   ├── ScreenshotTool.java
│   │   ├── RetryEngineTool.java
│   │   ├── RecoveryEngineTool.java
│   │   └── SessionManagerTool.java
│   ├── discovery/
│   │   ├── JobDiscoveryTool.java
│   │   └── JobDeduplicationTool.java
│   └── storage/
│       ├── ResumeStorageTool.java
│       ├── JobStorageTool.java
│       ├── ApplicationStorageTool.java
│       └── ScreenshotStorageTool.java
├── memory/
│   ├── LongTermMemory.java
│   ├── ShortTermMemory.java
│   ├── KnowledgeStore.java
│   └── EpisodeMemory.java
├── planning/
│   ├── Planner.java
│   ├── TaskPlanner.java
│   └── WorkflowEngine.java
├── reasoning/
│   ├── Reasoner.java
│   └── DecisionEngine.java
├── queue/
│   ├── TaskQueue.java
│   └── PriorityQueue.java
├── observation/
│   ├── ObservationEngine.java
│   └── StateMonitor.java
└── notification/
    ├── NotificationEngine.java
    └── AlertManager.java
```

### 4.2 Domain Code Diagram

```
com.jobpilot.domain/
├── mission/
│   ├── Mission.java
│   ├── MissionId.java
│   ├── MissionStatus.java
│   └── MissionMetrics.java
├── candidate/
│   ├── CandidateProfile.java
│   ├── CandidateId.java
│   ├── Skill.java
│   ├── Experience.java
│   ├── Education.java
│   └── Certification.java
├── job/
│   ├── JobListing.java
│   ├── JobId.java
│   ├── JobAnalysis.java
│   └── CompatibilityScore.java
├── application/
│   ├── Application.java
│   ├── ApplicationId.java
│   ├── ApplicationStatus.java
│   └── AutomationResult.java
├── memory/
│   ├── Memory.java
│   ├── MemoryId.java
│   ├── MemoryType.java
│   └── Episode.java
├── task/
│   ├── Task.java
│   ├── TaskId.java
│   ├── TaskType.java
│   └── TaskStatus.java
├── agent/
│   ├── AgentState.java
│   ├── AgentId.java
│   ├── AgentStatus.java
│   └── AgentPhase.java
└── shared/
    ├── BaseAggregateRoot.java
    ├── Money.java
    └── Email.java
```

### 4.3 Key Class Relationships

**Agent Loop:**
```
AgentLoop
    ├── uses → ObservePhase
    ├── uses → ThinkPhase
    ├── uses → PlanPhase
    ├── uses → ExecutePhase
    ├── uses → VerifyPhase
    └── uses → LearnPhase

ExecutePhase
    ├── uses → Tool Layer
    │   ├── uses → AI Tools
    │   ├── uses → Browser Tools
    │   ├── uses → Discovery Tools
    │   └── uses → Storage Tools
    └── uses → TaskQueue
```

**Mission Aggregate:**
```
Mission (Aggregate Root)
    ├── contains → MissionMetrics (Value Object)
    └── has → List<Task> (Entity)
```

**Agent State Aggregate:**
```
AgentState (Aggregate Root)
    ├── references → Mission (Entity)
    └── contains → Map<String, Object> context
```

---

**End of C4 Architecture v2.0**
