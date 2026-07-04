# JobPilot AI v2.0 — Software Requirements Specification (SRS)

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
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

JobPilot AI v2.0 is an **offline-first autonomous AI job agent** that automates the entire job application workflow. Unlike traditional job portals (LinkedIn, Indeed) that require manual searching and application, JobPilot AI acts as an intelligent employee whose sole job is getting interviews for the user.

The system is built around a **Mission-driven Agent Runtime** that continuously observes, thinks, plans, executes, verifies, and learns. Users define a Mission (job hunting goal with preferences), and the agent autonomously searches job boards, analyzes opportunities, tailors resumes, generates cover letters, fills application forms, and submits applications—all while the user monitors progress through a Mission Control interface.

The system is **offline-first** by default, using Ollama (local LLM) for all AI reasoning. Cloud AI providers (OpenAI, Gemini, Claude) are optional plugins. The architecture is agent-centric, not page-centric or CRUD-centric.

---

## 2. Business Problem

### 2.1 The Problem Space

- **Manual job hunting is broken:** Candidates spend hours daily searching LinkedIn, Indeed, company portals, filling redundant forms, tailoring resumes, and writing cover letters. This is repetitive, error-prone, and soul-crushing.
- **Missed opportunities:** Jobs appear and disappear daily. No human can monitor all sources 24/7.
- **Application burnout:** Sending 100+ applications with low response rates leads to frustration and abandonment.
- **No intelligent automation:** Existing tools (Simplify.jobs, Huntr) only autofill forms. They don't reason about job fit, tailor content, or learn from outcomes.
- **Privacy concerns:** Cloud-based AI services process sensitive personal data (resumes, career history) on remote servers.
- **No continuous improvement:** Humans don't learn from every application. An AI agent can remember what worked, what didn't, and improve over time.

### 2.2 Market Gap

Existing tools address isolated pieces:
- **LinkedIn/Indeed** — Job aggregation, no automation
- **Simplify.jobs** — Form autofill, no AI reasoning
- **Huntr/Jopwell** — Application tracking, no automation
- **Resume.io** — Resume builder, no automation
- **ChatGPT** — AI assistance, but no automation or memory

No platform combines **offline-first AI reasoning, autonomous browser automation, long-term memory, and continuous learning** into a unified agent that works 24/7 without user intervention.

---

## 3. Vision, Mission & Goals

### 3.1 Vision

To become the world's best open-source autonomous AI job agent—an intelligent employee that works 24/7 to get interviews for its users.

### 3.2 Mission

Eliminate the friction of job hunting by building an offline-first autonomous AI agent that continuously searches, analyzes, tailors, and applies to jobs while learning from every interaction.

### 3.3 Strategic Goals

| Goal | Description | Timeline |
|------|-------------|----------|
| G1 | Launch MVP with complete agent loop (resume → mission → search → analyze → apply) | Phase 1 |
| G2 | Achieve 90%+ application success rate on LinkedIn Easy Apply | Phase 2 |
| G3 | Implement long-term memory with learning from outcomes | Phase 3 |
| G4 | Support 5+ job board adapters (LinkedIn, Indeed, Greenhouse, Lever, Workday) | Phase 4 |
| G5 | Implement natural language chat interface for agent control | Phase 5 |
| G6 | Achieve offline-first operation with Ollama by default | Phase 1 |

---

## 4. Target Users & Personas

### 4.1 Primary Persona: Alex the Software Engineer

**Profile:**
- 28-year-old software engineer with 3 years experience
- Currently employed but seeking better opportunities
- Values time and hates repetitive tasks
- Privacy-conscious, prefers local data processing
- Comfortable with technology but not an AI expert

**Pain Points:**
- Spends 2-3 hours daily on job hunting
- Low response rate from applications
- Forgets to follow up on applications
- Doesn't know which resume version worked best
- Misses jobs because can't monitor all sources 24/7

**Goals:**
- Automate repetitive job hunting tasks
- Get more interviews with less effort
- Maintain privacy of personal data
- Learn what works and what doesn't

### 4.2 Secondary Persona: Jordan the Career Changer

**Profile:**
- 35-year-old marketing professional transitioning to tech
- Limited technical skills
- Overwhelmed by job hunting process
- Needs guidance on what to apply for

**Pain Points:**
- Doesn't know which jobs are realistic matches
- Struggles with tailoring resume for different roles
- Doesn't know how to write effective cover letters
- Misses opportunities due to slow response time

**Goals:**
- AI to identify suitable opportunities
- Automated resume tailoring
- Guidance on application strategy
- Continuous improvement based on feedback

---

## 5. Functional Requirements

### 5.1 Agent Runtime (Core)

**FR-1: Agent Loop**
- The agent must continuously execute: Observe → Think → Plan → Execute → Verify → Learn → Repeat
- The agent must pause when user requests or when encountering CAPTCHA/MFA
- The agent must resume automatically after pause conditions are resolved
- The agent must stop when mission is complete, deadline reached, or user stops

**FR-2: Mission Management**
- Users must be able to create Missions with: name, goal, target salary, locations, experience, preferred roles, preferred companies, companies to avoid, daily apply limit, deadline
- Users must be able to start, pause, resume, and stop Missions
- Users must be able to modify Mission parameters while agent is running
- The system must track Mission metrics: jobs found, jobs analyzed, applications submitted, interviews scheduled, offers received

**FR-3: Memory System**
- The agent must maintain long-term memory of user preferences (e.g., "Never apply to TCS")
- The agent must maintain short-term memory of current context (e.g., "Currently applying to Adobe")
- The agent must maintain knowledge store of learned strategies (e.g., "LinkedIn Easy Apply works best on Tuesdays")
- The agent must maintain episode memory of complete application cycles
- Memory must persist across agent restarts

### 5.2 AI Tools

**FR-4: Resume Parsing**
- The agent must parse uploaded resumes (PDF, DOCX) and extract: skills, experience, education, certifications, summary
- The agent must identify skill proficiency levels from experience descriptions
- The agent must detect skill gaps compared to job requirements

**FR-5: Job Analysis**
- The agent must analyze job descriptions and extract: required skills, preferred skills, qualifications, salary range, location, company type
- The agent must compute compatibility score between candidate profile and job description
- The agent must detect scam jobs (e.g., unrealistic salary, vague description, requests for payment)
- The agent must estimate interview probability based on profile match

**FR-6: Resume Tailoring**
- The agent must tailor resumes for specific job descriptions by:
  - Reordering skills to match job requirements
  - Emphasizing relevant experience
  - Adding missing keywords from job description
  - Adjusting summary to align with role
- The agent must maintain multiple resume versions (one per job application)

**FR-7: Cover Letter Generation**
- The agent must generate job-specific cover letters based on:
  - Candidate profile
  - Job description
  - Company research
  - User-selected tone (professional, casual, enthusiastic)
- The agent must customize cover letters for each application

**FR-8: Answer Generation**
- The agent must generate answers for application questions (e.g., "Why do you want to work here?", "Describe a challenge you overcame")
- The agent must ensure answers are consistent with candidate profile

**FR-9: Job Ranking**
- The agent must rank discovered jobs by compatibility score
- The agent must filter out jobs below minimum compatibility threshold
- The agent must prioritize jobs based on user preferences (salary, location, company type)

### 5.3 Browser Automation

**FR-10: Generic Automation Framework**
- The system must provide a generic browser automation framework (not site-specific)
- The framework must include: DOM analyzer, page classifier, action planner, form engine, upload engine, question engine, screenshot engine, retry engine, recovery engine, session manager

**FR-11: Site Adapters**
- The system must provide site-specific adapters containing only selectors and workflow rules
- Initial adapters: LinkedIn, Indeed, Greenhouse, Lever, Workday
- Adapters must implement: search jobs, open jobs, login, fill forms, upload resume, upload cover letter, answer questions, submit, capture screenshot

**FR-12: CAPTCHA/MFA Handling**
- The agent must detect CAPTCHA and MFA challenges
- The agent must pause and notify user when CAPTCHA/MFA is encountered
- The agent must resume after user manually completes CAPTCHA/MFA
- The agent must take screenshot before pausing for user verification

**FR-13: User Approval**
- The system must support configurable approval rules:
  - Auto-submit all applications
  - Require approval before submission
  - Require approval only for high-value applications
- The system must display application preview (filled form, tailored resume, cover letter) before submission
- The system must allow user to modify content before submission

### 5.4 Job Discovery

**FR-14: Multi-Source Discovery**
- The agent must search multiple job boards simultaneously
- The agent must remove duplicate job listings across sources
- The agent must respect rate limits per job board
- The agent must schedule discovery runs based on user preferences (e.g., "Search every 6 hours")

**FR-15: Search Criteria**
- The agent must search based on Mission parameters: roles, locations, salary range, experience level, company preferences
- The agent must adapt search queries per job board (different search syntax)
- The agent must filter results post-search to ensure they match Mission criteria

### 5.5 Mission Control (Frontend)

**FR-16: Agent Status Display**
- The system must display: agent status (idle/running/paused/stopped), current task, progress percentage, current phase (observe/think/plan/execute/verify/learn)
- The system must display real-time metrics: jobs found, jobs analyzed, high match jobs, applications submitted, applications waiting, errors

**FR-17: Timeline**
- The system must display a live timeline of agent actions
- The system must show: task started, task completed, errors, warnings, user interventions
- The system must allow filtering by severity and time range

**FR-18: Log Console**
- The system must display agent logs in real-time
- The system must support log levels: debug, info, warn, error
- The system must allow filtering and searching logs

**FR-19: Control Buttons**
- The system must provide large START, PAUSE, STOP buttons
- The system must disable buttons based on current agent state
- The system must require confirmation before stopping

**FR-20: Chat Interface**
- The system must provide natural language chat interface for agent control
- Supported commands: "Find remote Java backend jobs", "Pause until tomorrow", "Increase salary target", "Skip service companies", "Show today's applications"
- The system must understand intent and execute corresponding agent actions

### 5.6 Candidate Profile

**FR-21: Profile Management**
- Users must be able to upload resumes (PDF, DOCX)
- The system must automatically parse and extract profile data
- Users must be able to edit extracted data
- Users must be able to add skills with proficiency levels
- Users must be able to add experience and education

**FR-22: Preferences**
- Users must be able to set job preferences: target roles, salary range, locations, remote preference, company type, companies to avoid
- Preferences must be used in Mission creation
- Preferences must be stored in long-term memory

### 5.7 Application Tracking

**FR-23: Read-Only Tracking**
- The system must display applications submitted by the agent (read-only)
- The system must show: company, role, resume used, cover letter used, submission time, status, screenshots
- The system must allow users to view application details and screenshots
- Users must NOT be able to manually create or edit applications

**FR-24: Status Updates**
- The system must track application status: submitted, under review, interview scheduled, offer, rejected
- When status changes are detected (e.g., email notification), the system must update application status
- The system must notify user of status changes

---

## 6. Non-Functional Requirements

### 6.1 Performance

**NFR-1: Agent Loop Latency**
- Agent loop iteration must complete within 30 seconds for normal operations
- AI inference (Ollama) must complete within 10 seconds for typical prompts
- Browser automation actions must complete within 5 seconds per action

**NFR-2: Response Time**
- Mission Control UI must update within 1 second of agent state change
- Chat interface must respond within 3 seconds
- WebSocket messages must deliver within 500ms

### 6.2 Reliability

**NFR-3: Agent Recovery**
- Agent must resume from last known state after crash/restart
- Agent must not lose memory or task queue on restart
- Agent must retry failed tasks with exponential backoff

**NFR-4: Browser Automation**
- Browser automation must handle network failures gracefully
- Browser automation must recover from page load failures
- Browser automation must handle session timeouts

### 6.3 Scalability

**NFR-5: Concurrent Users**
- System must support 100+ concurrent agents (single-user deployment: not applicable)
- System must support 10+ concurrent job board scrapers per agent

**NFR-6: Data Volume**
- System must store 10,000+ job listings per user
- System must store 1,000+ applications per user
- System must store unlimited memory entries per user

### 6.4 Security

**NFR-7: Data Privacy**
- All user data must be stored locally by default
- AI inference must run locally (Ollama) by default
- Cloud AI providers must be opt-in only
- User data must never be sent to third parties without explicit consent

**NFR-8: Authentication**
- System must require authentication for all operations
- System must use JWT tokens with refresh token rotation
- System must support password reset and email verification

### 6.5 Usability

**NFR-9: Offline Operation**
- System must work completely offline with Ollama installed
- System must guide user through Ollama installation if not detected
- System must function without internet connection (except for job board scraping)

**NFR-10: Ease of Use**
- User must be able to create first Mission within 5 minutes
- User must be able to understand agent status without technical knowledge
- Chat interface must understand natural language without specific syntax

### 6.6 Maintainability

**NFR-11: Code Quality**
- Code must follow Clean Architecture principles
- Code must have 90%+ test coverage for domain logic
- Code must pass ArchUnit architecture tests

**NFR-12: Documentation**
- All modules must have updated documentation
- All APIs must have OpenAPI specifications
- All domain entities must have Javadoc

---

## 7. Modules & Features

### 7.1 Agent Runtime (NEW - CORE)

**Purpose:** The heart of the system. Orchestrates the autonomous agent loop.

**Features:**
- Agent Loop (Observe-Think-Plan-Execute-Verify-Learn)
- Tool Layer (AI tools, Browser tools, Discovery tools, Storage tools)
- Memory Layer (Long-term, Short-term, Knowledge store, Episode memory)
- Planning Layer (Planner, Task planner, Workflow engine)
- Reasoning Layer (Reasoner, Decision engine)
- Task Queue (Priority queue, Task scheduling)
- Observation Engine (State monitoring, Event detection)
- Notification Engine (Alert manager, Notification dispatch)

### 7.2 AI Provider Layer (REFACTOR)

**Purpose:** Abstract interface for AI inference. Ollama-first, cloud optional.

**Features:**
- Ollama provider (default, local)
- OpenAI provider (optional, cloud)
- Gemini provider (optional, cloud)
- Claude provider (optional, cloud)
- Auto-detection of Ollama installation
- Model selection (Llama 3.x, Qwen 2.5, Mistral, DeepSeek, Gemma)

### 7.3 Browser Automation Framework (NEW)

**Purpose:** Generic browser automation framework with site-specific adapters.

**Features:**
- Browser Manager (Playwright integration)
- DOM Analyzer (Element detection, classification)
- Page Classifier (Page type identification)
- Action Planner (Action sequence planning)
- Form Engine (Form filling)
- Upload Engine (File upload)
- Question Engine (Question answering)
- Screenshot Engine (Screenshot capture)
- Retry Engine (Retry logic)
- Recovery Engine (Error recovery)
- Session Manager (Session persistence)
- Site Adapters (LinkedIn, Indeed, Greenhouse, Lever, Workday)

### 7.4 Mission Management (NEW)

**Purpose:** User-defined job hunting goals.

**Features:**
- Mission creation (name, goal, salary, locations, experience, roles, companies, limits, deadline)
- Mission control (start, pause, resume, stop)
- Mission metrics (jobs found, analyzed, applied, interviews, offers)
- Mission history (past missions, outcomes)

### 7.5 Candidate Profile (NEW - REFACTOR from User Profile)

**Purpose:** User's professional profile extracted from resume.

**Features:**
- Resume upload and parsing
- Profile editing (skills, experience, education)
- Skill proficiency tracking
- Preference management (roles, salary, locations, companies)

### 7.6 Job Discovery (REFACTOR)

**Purpose:** Agent-driven job discovery from multiple sources.

**Features:**
- Multi-source job board search
- Job deduplication
- Search criteria based on Mission
- Rate limiting per source
- Scheduling and retry

### 7.7 Application Tracking (REFACTOR - Read-Only)

**Purpose:** Read-only display of agent-submitted applications.

**Features:**
- Application list (read-only)
- Application details (screenshots, resume, cover letter)
- Status tracking (submitted, under review, interview, offer, rejected)
- Status change notifications

### 7.8 Mission Control (NEW - Frontend)

**Purpose:** Agent supervision interface.

**Features:**
- Agent status display
- Current task display
- Progress panel
- Stats panel
- Timeline
- Log console
- Control buttons (START/PAUSE/STOP)
- Chat interface

### 7.9 Authentication (KEEP)

**Purpose:** User authentication and authorization.

**Features:**
- JWT-based authentication
- Refresh token rotation
- Password reset
- Email verification
- Role-based access control

### 7.10 Notification Service (KEEP - SIMPLIFIED)

**Purpose:** Agent alerts to user.

**Features:**
- In-app notifications
- Email notifications (optional)
- Alert types: CAPTCHA detected, application submitted, status change, error, mission complete

---

## 8. User Stories

### 8.1 Mission Stories

**US-1:** As a job seeker, I want to create a Mission with my job hunting preferences so that the agent knows what to search for.

**US-2:** As a job seeker, I want to start the agent so that it begins searching and applying to jobs automatically.

**US-3:** As a job seeker, I want to pause the agent so that I can review its progress or make changes.

**US-4:** As a job seeker, I want to stop the agent so that it stops applying to jobs.

**US-5:** As a job seeker, I want to view Mission metrics so that I can see how many jobs were found, analyzed, and applied to.

### 8.2 Profile Stories

**US-6:** As a job seeker, I want to upload my resume so that the agent can extract my profile.

**US-7:** As a job seeker, I want to edit my extracted profile so that the agent has accurate information.

**US-8:** As a job seeker, I want to set my job preferences so that the agent searches for relevant jobs.

### 8.3 Agent Control Stories

**US-9:** As a job seeker, I want to see the agent's current status so that I know what it's doing.

**US-10:** As a job seeker, I want to see the agent's current task so that I know its progress.

**US-11:** As a job seeker, I want to see a timeline of agent actions so that I can review its activity.

**US-12:** As a job seeker, I want to use natural language to control the agent so that I don't need to learn specific commands.

### 8.4 Application Stories

**US-13:** As a job seeker, I want to view applications submitted by the agent so that I can track progress.

**US-14:** As a job seeker, I want to see screenshots of submitted applications so that I can verify what was sent.

**US-15:** As a job seeker, I want to receive notifications when application status changes so that I can respond quickly.

### 8.5 Approval Stories

**US-16:** As a job seeker, I want to approve applications before submission so that I can control what gets sent.

**US-17:** As a job seeker, I want to modify application content before submission so that I can correct errors.

**US-18:** As a job seeker, I want to configure auto-approval rules so that I don't have to approve every application.

---

## 9. Use Cases

### UC-1: Create Mission

**Actor:** Job Seeker

**Preconditions:** User is logged in, has uploaded resume

**Main Flow:**
1. User navigates to Mission creation page
2. User enters Mission details: name, goal, target salary, locations, experience, preferred roles, preferred companies, companies to avoid, daily apply limit, deadline
3. System validates input
4. System creates Mission entity
5. System stores Mission in database
6. System displays Mission confirmation

**Postconditions:** Mission is created and ready to start

### UC-2: Start Agent

**Actor:** Job Seeker

**Preconditions:** Mission exists, agent is idle

**Main Flow:**
1. User clicks START button
2. System validates Mission has required parameters
3. System initializes Agent Runtime
4. System starts Agent Loop
5. System updates agent status to RUNNING
6. System notifies user via WebSocket

**Postconditions:** Agent is running and executing Mission

### UC-3: Agent Loop Execution

**Actor:** Agent Runtime

**Preconditions:** Agent is RUNNING, Mission exists

**Main Flow:**
1. **Observe:** Agent checks Mission status, task queue, memory, external state
2. **Think:** Agent uses AI to reason about current state and progress
3. **Plan:** Agent breaks down Mission into tasks, prioritizes, creates execution plan
4. **Execute:** Agent executes tasks using tools (AI tools, Browser tools, Discovery tools)
5. **Verify:** Agent verifies task completion, checks for errors
6. **Learn:** Agent updates memory with results, refines strategies
7. **Repeat:** Agent returns to Observe phase

**Postconditions:** Agent continues loop until Mission complete, deadline, or user stop

### UC-4: Pause Agent

**Actor:** Job Seeker

**Preconditions:** Agent is RUNNING

**Main Flow:**
1. User clicks PAUSE button
2. System stops Agent Loop after current task completes
3. System updates agent status to PAUSED
4. System saves current state
5. System notifies user via WebSocket

**Postconditions:** Agent is paused, state is saved

### UC-5: Resume Agent

**Actor:** Job Seeker

**Preconditions:** Agent is PAUSED

**Main Flow:**
1. User clicks RESUME button
2. System loads saved state
3. System restarts Agent Loop
4. System updates agent status to RUNNING
5. System notifies user via WebSocket

**Postconditions:** Agent is running from saved state

### UC-6: Chat Control

**Actor:** Job Seeker

**Preconditions:** Agent is running or paused

**Main Flow:**
1. User types natural language command in chat interface
2. System sends command to Agent Runtime
3. Agent uses AI to understand intent
4. Agent executes corresponding action
5. System responds with confirmation
6. System updates UI if needed

**Postconditions:** Command is executed, user is notified

### UC-7: CAPTCHA Handling

**Actor:** Agent Runtime

**Preconditions:** Agent is filling application form

**Main Flow:**
1. Agent detects CAPTCHA challenge
2. Agent takes screenshot
3. Agent pauses Agent Loop
4. Agent updates agent status to AWAITING_USER
5. Agent notifies user via WebSocket with screenshot
6. User manually completes CAPTCHA
7. User clicks RESUME
8. Agent resumes Agent Loop
9. Agent continues application submission

**Postconditions:** CAPTCHA is resolved, application continues

---

## 10. Acceptance Criteria

### AC-1: Mission Creation

- Given a logged-in user with uploaded resume
- When user creates a Mission with valid parameters
- Then Mission is created and stored in database
- And Mission ID is generated
- And Mission status is DRAFT

### AC-2: Agent Start

- Given a Mission with valid parameters
- When user clicks START button
- Then Agent status changes to RUNNING
- And Agent Loop begins execution
- And WebSocket notification is sent
- And Mission status changes to ACTIVE

### AC-3: Job Discovery

- Given an active Mission with search criteria
- When Agent executes job discovery task
- Then Agent searches configured job boards
- And Agent removes duplicate listings
- And Agent filters by Mission criteria
- And Agent stores matching jobs in database
- And Agent updates Mission metrics

### AC-4: Job Analysis

- Given discovered jobs
- When Agent executes job analysis task
- Then Agent analyzes each job description
- And Agent computes compatibility score
- And Agent detects scam jobs
- And Agent ranks jobs by compatibility
- And Agent filters out jobs below threshold

### AC-5: Resume Tailoring

- Given a high-match job
- When Agent executes resume tailoring task
- Then Agent tailors resume for job description
- And Agent reorders skills to match requirements
- And Agent emphasizes relevant experience
- And Agent adds missing keywords
- And Agent saves tailored resume version

### AC-6: Cover Letter Generation

- Given a high-match job and tailored resume
- When Agent executes cover letter generation task
- Then Agent generates job-specific cover letter
- And Agent customizes based on company research
- And Agent uses selected tone
- And Agent saves cover letter

### AC-7: Application Submission

- Given tailored resume and cover letter
- When Agent executes application submission task
- And approval rule requires approval
- Then Agent displays application preview
- And Agent waits for user approval
- When user approves
- Then Agent fills application form
- And Agent uploads resume and cover letter
- And Agent answers questions
- And Agent submits application
- And Agent takes screenshot
- And Agent stores application result
- And Agent updates Mission metrics

### AC-8: Memory Update

- Given a completed task with result
- When Agent executes learn phase
- Then Agent updates long-term memory with preferences
- And Agent updates short-term memory with context
- And Agent updates knowledge store with strategies
- And Agent creates episode memory
- And Agent persists all memory

### AC-9: CAPTCHA Detection

- Given Agent is filling application form
- When CAPTCHA is detected
- Then Agent takes screenshot
- And Agent pauses Agent Loop
- And Agent updates agent status to AWAITING_USER
- And Agent notifies user with screenshot
- And Agent waits for user to complete CAPTCHA

### AC-10: Chat Command

- Given user types "Find remote Java backend jobs"
- When Agent receives command
- Then Agent understands intent
- And Agent updates Mission search criteria
- And Agent executes job discovery
- And Agent responds with confirmation

---

## 11. Business Rules

### BR-1: Mission Rules

- A user can have only one active Mission at a time
- Mission must have at least one target role and one location
- Mission daily apply limit cannot exceed 50
- Mission deadline cannot be less than 7 days from creation

### BR-2: Agent Rules

- Agent cannot apply to companies in user's avoid list
- Agent cannot apply to jobs below minimum compatibility threshold (configurable, default 70%)
- Agent must respect daily apply limit
- Agent must pause for CAPTCHA/MFA
- Agent must stop when Mission is complete or deadline reached

### BR-3: Memory Rules

- Long-term memory entries must have confidence score
- Memory entries with confidence below 0.5 must be archived
- Episode memory must be created for each complete application cycle
- Knowledge store must be updated only after successful outcomes

### BR-4: Browser Automation Rules

- Browser automation must use human-like delays (2-5 seconds between actions)
- Browser automation must retry failed actions up to 3 times
- Browser automation must take screenshot before each submission
- Browser automation must not store credentials (user must provide per session)

### BR-5: AI Rules

- AI inference must use Ollama by default
- Cloud AI providers must be opt-in only
- AI prompts must not include PII beyond what's necessary
- AI responses must be cached for identical requests

### BR-6: Application Rules

- Applications cannot be manually created or edited
- Application status can only be updated by agent or external notifications
- Application screenshots must be stored for at least 90 days
- Application data must not be shared with third parties

---

## 12. System Constraints

### SC-1: Technology Constraints

- Backend must use Java 21 and Spring Boot 3.3+
- Frontend must use Next.js 14 and TypeScript
- Database must be PostgreSQL 16+
- AI must use Ollama by default (local)
- Browser automation must use Playwright Java

### SC-2: Deployment Constraints

- System must be deployable via Docker
- System must support offline operation (no internet required for core functionality)
- System must run on consumer hardware (minimum 8GB RAM, 4 CPU cores)

### SC-3: Data Constraints

- All user data must be stored locally by default
- User data must be encrypted at rest
- User data must not be sent to cloud AI without explicit consent

### SC-4: Legal Constraints

- System must comply with data privacy regulations (GDPR, CCPA)
- System must provide data export functionality
- System must provide data deletion functionality

---

## 13. Technology Stack

### Backend

- **Language:** Java 21
- **Framework:** Spring Boot 3.3.5
- **Architecture:** Clean Architecture (Hexagonal)
- **Database:** PostgreSQL 16 with pgvector extension
- **Cache:** Redis 7
- **AI:** Ollama (default), OpenAI/Gemini/Claude (optional)
- **Browser Automation:** Playwright Java
- **Build:** Maven
- **Testing:** JUnit 5, Mockito, TestContainers, ArchUnit

### Frontend

- **Framework:** Next.js 14 (App Router)
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **UI Components:** Radix UI
- **State Management:** Zustand
- **Data Fetching:** React Query
- **Real-time:** WebSocket
- **Testing:** Jest, Playwright

### Infrastructure

- **Containerization:** Docker
- **Orchestration:** Docker Compose (dev), Kubernetes (prod optional)
- **Monitoring:** Prometheus, Grafana
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing:** OpenTelemetry, Jaeger

---

## 14. Architecture Overview

### 14.1 Architectural Philosophy

JobPilot AI v2.0 follows **Agent-Centric Architecture**. The entire system revolves around the Agent Runtime. The web application is merely a control center for supervising the autonomous agent.

### 14.2 Core Principles

| Principle | Application |
|-----------|-------------|
| **Agent-Centric** | All functionality flows through the Agent Runtime |
| **Offline-First** | Default operation is local, cloud is optional |
| **Mission-Driven** | Users define Missions, agent executes autonomously |
| **Memory-Persistent** | Agent learns and remembers across sessions |
| **Tool-Based** | Agent capabilities are composable tools |
| **Clean Architecture** | Domain → Application → Infrastructure → Interfaces |

### 14.3 High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Mission Control (Frontend)              │
│  Agent Status | Current Task | Progress | Timeline |   │
│  Control Buttons | Chat Interface | Log Console        │
└────────────────────────┬────────────────────────────────┘
                         │ WebSocket
┌────────────────────────▼────────────────────────────────┐
│                  Agent Runtime (Backend)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Agent Loop   │  │ Tool Layer   │  │ Memory Layer │  │
│  │ Observe      │  │ AI Tools     │  │ Long-term    │  │
│  │ Think        │  │ Browser Tools│  │ Short-term   │  │
│  │ Plan         │  │ Discovery    │  │ Knowledge    │  │
│  │ Execute      │  │ Storage      │  │ Episode      │  │
│  │ Verify       │  │              │  │              │  │
│  │ Learn        │  │              │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  AI Provider Layer                       │
│  Ollama (default) | OpenAI (opt) | Gemini (opt) | Claude│
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│              Browser Automation Framework                 │
│  Generic Framework | Site Adapters (LinkedIn, Indeed...)│
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│              Infrastructure Layer                        │
│  PostgreSQL | Redis | File Storage | Security | Config   │
└───────────────────────────────────────────────────────────┘
```

---

## 15. AI Architecture

### 15.1 AI Provider Layer

The AI Provider Layer abstracts AI inference behind a common interface. Ollama is the default provider (local, offline). Cloud providers are optional plugins.

**Interface:**
```java
public interface AiProvider {
    AiResponse generateText(AiRequest request);
    Flux<AiChunk> generateStream(AiRequest request);
    List<Float> generateEmbedding(String text);
    int countTokens(String text);
    boolean isAvailable();
}
```

**Providers:**
- **OllamaProvider:** Local LLM inference (Llama 3.x, Qwen 2.5, Mistral, DeepSeek, Gemma)
- **OpenAIProvider:** OpenAI GPT-4/GPT-3.5 (optional, cloud)
- **GeminiProvider:** Google Gemini (optional, cloud)
- **ClaudeProvider:** Anthropic Claude (optional, cloud)

**Auto-Detection:**
- System checks for Ollama at `http://localhost:11434` on startup
- If Ollama unavailable, system guides user through installation
- User can manually configure cloud providers as fallback

### 15.2 AI Tools

AI Tools are domain-specific AI capabilities used by the Agent Runtime:

**ResumeParserTool:** Extracts skills, experience, education from resume
**JobAnalyzerTool:** Analyzes job description, extracts requirements, computes compatibility
**ResumeTailorTool:** Tailors resume for specific job description
**CoverLetterTool:** Generates job-specific cover letters
**AnswerGeneratorTool:** Generates answers for application questions
**JobRankerTool:** Ranks jobs by compatibility score
**ScamDetectorTool:** Detects scam jobs
**SkillGapTool:** Identifies skill gaps between profile and job

### 15.3 Prompt Engineering

Prompts are stored in `prompt_templates` table for versioning and A/B testing. System uses few-shot learning with examples stored in knowledge store.

---

## 16. Browser Automation Architecture

### 16.1 Generic Framework

The Browser Automation Framework is generic and not site-specific. Site-specific logic is isolated in adapters.

**Components:**
- **BrowserManager:** Manages Playwright browser instances
- **DOMAnalyzer:** Analyzes DOM structure, detects elements
- **PageClassifier:** Classifies page type (login, form, listing, etc.)
- **ActionPlanner:** Plans action sequence based on page type
- **FormEngine:** Fills form fields intelligently
- **UploadEngine:** Uploads files (resume, cover letter)
- **QuestionEngine:** Answers application questions
- **ScreenshotEngine:** Captures screenshots
- **RetryEngine:** Retries failed actions with exponential backoff
- **RecoveryEngine:** Recovers from errors (session timeout, network failure)
- **SessionManager:** Manages browser sessions and cookies

### 16.2 Site Adapters

Site adapters contain only selectors and workflow rules. The framework handles everything else.

**Adapter Interface:**
```java
public interface SiteAdapter {
    String siteName();
    List<JobListing> searchJobs(SearchCriteria criteria);
    void openJob(JobListing job);
    void login(Credentials credentials);
    void fillForm(ApplicationData data);
    void uploadResume(File resume);
    void uploadCoverLetter(File coverLetter);
    void answerQuestions(List<Question> questions);
    void submit();
    String takeScreenshot();
    boolean detectCaptcha();
}
```

**Adapters:**
- **LinkedInAdapter:** LinkedIn Easy Apply
- **IndeedAdapter:** Indeed application flow
- **GreenhouseAdapter:** Greenhouse ATS
- **LeverAdapter:** Lever ATS
- **WorkdayAdapter:** Workday ATS

### 16.3 CAPTCHA/MFA Handling

- Agent detects CAPTCHA via DOM analysis
- Agent takes screenshot
- Agent pauses and notifies user
- User manually completes CAPTCHA
- Agent resumes after user confirmation

---

## 17. Security Architecture

### 17.1 Authentication

- JWT-based authentication with refresh token rotation
- Password hashing with BCrypt
- Email verification for registration
- Password reset via email token

### 17.2 Authorization

- Role-based access control (USER, ADMIN)
- Method-level security with @PreAuthorize
- Resource-level security (users can only access their own data)

### 17.3 Data Privacy

- All user data encrypted at rest (AES-256)
- AI inference runs locally (Ollama) by default
- Cloud AI opt-in only
- No data sharing with third parties without consent

### 17.4 Rate Limiting

- API rate limiting per user (100 requests/minute)
- Job board scraping rate limiting per source
- Browser automation rate limiting per domain

---

## 18. Data Model (Conceptual)

### 18.1 Core Entities

**Mission:** User's job hunting goal
- MissionId, UserId, Name, Goal, TargetSalary, Locations, Experience, PreferredRoles, PreferredCompanies, AvoidCompanies, DailyApplyLimit, Deadline, Status, Metrics

**CandidateProfile:** User's professional profile
- CandidateId, UserId, FullName, Email, Phone, Location, Skills, Experiences, Educations, Certifications, Summary, CurrentSalary, ExpectedSalary, Preferences

**JobListing:** Job from external source
- JobId, Source, SourceId, Title, CompanyName, Description, Requirements, Salary, Location, PostedAt, IsActive

**JobAnalysis:** AI analysis of job
- JobAnalysisId, JobId, UserId, CompatibilityScore, ScoreBreakdown, MatchedSkills, MissingSkills, ScamDetection, InterviewProbability

**Application:** Agent-submitted application
- ApplicationId, UserId, JobId, ResumeId, CoverLetterId, Status, SubmittedAt, ScreenshotUrl, AutomationResult

**Memory:** Agent's memory
- MemoryId, UserId, Type, Key, Value, Metadata, Confidence, LastAccessed

**Task:** Agent task
- TaskId, MissionId, Type, Description, Status, Priority, Parameters, Result, ErrorMessage

**AgentState:** Current agent state
- AgentId, UserId, MissionId, Status, CurrentPhase, CurrentTaskId, LoopIteration, Context, RecentThoughts

### 18.2 Relationships

- User 1:N Mission
- User 1:1 CandidateProfile
- Mission 1:N Task
- Mission 1:N Application
- User 1:N Memory
- JobListing 1:1 JobAnalysis
- JobListing 1:N Application

---

## 19. API Design Philosophy

### 19.1 RESTful Design

- Resource-based URLs (e.g., /api/v1/missions, /api/v1/applications)
- HTTP verbs for operations (GET, POST, PUT, DELETE)
- JSON request/response
- Standard HTTP status codes

### 19.2 WebSocket

- Real-time agent status updates
- Real-time log streaming
- Real-time notifications

### 19.3 OpenAPI

- All REST APIs documented with OpenAPI 3.0
- Auto-generated Swagger UI at /swagger-ui.html

---

## 20. Frontend Architecture

### 20.1 Technology Stack

- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS
- Radix UI
- Zustand (state management)
- React Query (data fetching)
- WebSocket (real-time)

### 20.2 Page Structure

- `/mission-control` - Agent supervision interface
- `/mission-control/chat` - Chat interface
- `/missions` - Mission management
- `/missions/[id]` - Mission detail
- `/candidate` - Candidate profile
- `/candidate/upload` - Resume upload
- `/applications` - Application tracking (read-only)
- `/applications/[id]` - Application detail

### 20.3 Component Structure

- `mission-control/` - Agent status, progress, timeline, logs
- `chat/` - Chat interface
- `missions/` - Mission cards, forms, progress
- `candidate/` - Profile editor, skills editor
- `applications/` - Application list, detail, screenshot viewer
- `ui/` - Reusable UI components

---

## 21. Backend Architecture

### 21.1 Module Structure

```
jobpilot-backend/
├── jobpilot-bootstrap/           # Application entry point
├── jobpilot-common/             # Shared utilities
├── jobpilot-domain/             # Domain entities
├── jobpilot-agent-runtime/      # Agent Runtime (NEW)
├── jobpilot-ai-provider/        # AI Provider Layer (NEW)
├── jobpilot-browser-automation/ # Browser Automation (NEW)
├── jobpilot-application/        # Application services
├── jobpilot-infrastructure/      # Persistence, external integrations
├── jobpilot-interfaces/        # REST controllers, WebSocket
```

### 21.2 Clean Architecture

- **Domain:** Entities, value objects, domain services, ports (interfaces)
- **Application:** Use cases, application services, DTOs
- **Infrastructure:** JPA repositories, external API clients, configuration
- **Interfaces:** REST controllers, WebSocket handlers, DTOs

### 21.3 Dependency Rules

- Interfaces → Application → Domain
- Infrastructure → Domain
- Application → Domain
- No circular dependencies

---

## 22. Deployment Strategy

### 22.1 Development

- Docker Compose for local development
- Services: jobpilot-api, postgres, redis
- Hot reload for frontend and backend

### 22.2 Production

- Docker containerization
- Kubernetes deployment (optional for single-user)
- PostgreSQL with pgvector
- Redis for caching
- Ollama for AI (local)

### 22.3 Offline Mode

- System must work without internet connection
- Ollama must be installed locally
- Job board scraping requires internet (optional)

---

## 23. Scaling Strategy

### 23.1 Single-User Deployment

- Default deployment is single-user (one agent per user)
- Scales vertically: more CPU, more RAM for AI inference
- Browser automation limited by job board rate limits

### 23.2 Multi-User Deployment (Future)

- Horizontal scaling of API servers
- Each user has isolated agent instance
- Shared PostgreSQL and Redis
- Per-user rate limiting

---

## 24. Testing Strategy

### 24.1 Unit Testing

- JUnit 5 + Mockito
- 90%+ coverage for domain logic
- ArchUnit for architecture rules

### 24.2 Integration Testing

- @SpringBootTest + TestContainers
- PostgreSQL and Redis testcontainers
- AI provider integration with WireMock

### 24.3 E2E Testing

- Playwright for frontend E2E
- Critical paths: mission creation, agent start, application submission

### 24.4 Agent Testing

- Mock AI provider for deterministic tests
- Mock browser automation for fast tests
- Integration tests with real Ollama (optional)

---

## 25. Monitoring & Observability

### 25.1 Metrics

- Agent metrics: loop duration, task success rate, memory size
- AI metrics: inference duration, token usage, cache hit ratio
- Browser metrics: automation success rate, CAPTCHA detection rate
- Business metrics: jobs found, applications submitted, interviews scheduled

### 25.2 Logging

- Structured JSON logging (Logback)
- MDC fields: traceId, userId, agentId, taskId
- Log levels: DEBUG, INFO, WARN, ERROR

### 25.3 Tracing

- OpenTelemetry for distributed tracing
- W3C Trace Context propagation
- Jaeger for trace visualization

---

## 26. Logging Strategy

### 26.1 Log Levels

- Agent Loop: INFO (phase transitions, task completions)
- AI Tools: INFO (inference calls, results)
- Browser Automation: INFO (actions, screenshots)
- Errors: ERROR (failures, exceptions)
- Security: WARN (auth failures, rate limits)

### 26.2 Log Format

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "level": "INFO",
  "logger": "com.jobpilot.agent.AgentLoop",
  "message": "Agent loop iteration completed",
  "mdc": {
    "traceId": "uuid",
    "userId": "uuid",
    "agentId": "uuid",
    "taskId": "uuid"
  }
}
```

---

## 27. Documentation Strategy

### 27.1 Documentation Types

- **SRS:** Software Requirements Specification (this document)
- **HLD:** High Level Design
- **LLD:** Low Level Design
- **C4:** C4 Architecture Diagrams
- **API:** OpenAPI Specifications
- **Module:** Per-module documentation

### 27.2 Documentation Tools

- **Markdown:** All documentation in Markdown
- **Mermaid:** Diagrams in Mermaid
- **OpenAPI:** API documentation
- **Javadoc:** Code documentation

---

## 28. Development Roadmap

### Phase 1: Foundation (Week 1-2)

- Create Agent Runtime module
- Implement AI Provider Layer (Ollama)
- Implement Browser Automation Framework
- Database migrations for Mission, Candidate, Memory, Task, AgentState

### Phase 2: Domain & Services (Week 3)

- Implement domain entities
- Implement Mission services
- Implement Candidate services
- Implement persistence

### Phase 3: Agent Tools (Week 4)

- Implement AI Tools (ResumeParser, JobAnalyzer, ResumeTailor, CoverLetter, JobRanker)
- Implement Discovery Tools
- Implement Storage Tools

### Phase 4: Agent Loop (Week 5)

- Implement Agent Loop phases
- Implement Memory system
- Implement Planning and Reasoning

### Phase 5: Browser Adapters (Week 6)

- Implement LinkedIn adapter
- Implement generic adapter framework
- Implement CAPTCHA detection

### Phase 6: Frontend Mission Control (Week 7)

- Implement Mission Control page
- Implement Chat interface
- Implement Mission management
- Implement Candidate profile

### Phase 7: First Complete Journey (Week 8)

- End-to-end integration
- Upload resume → Parse → Create Mission → Start Agent → Discover Jobs → Analyze → Tailor → Apply
- Testing and bug fixes

### Phase 8: Cleanup (Week 9)

- Remove job portal modules
- Remove mock data
- Update documentation

---

## 29. Future Scope

### FS-1: Additional Job Boards

- Glassdoor, Monster, CareerBuilder
- Company career pages (custom scrapers)

### FS-2: Advanced AI

- Interview preparation (mock interviews, question prediction)
- Salary negotiation advice
- Career path recommendations

### FS-3: Multi-Agent

- Multiple agents per user (e.g., one for full-time, one for contract)
- Agent collaboration

### FS-4: Mobile App

- React Native mobile app
- Push notifications

### FS-5: Community

- Shared knowledge store (anonymized)
- Agent strategy sharing

---

## 30. Risks & Mitigations

### Risk 1: Job Board Anti-Bot Measures

**Impact:** High - Job boards actively block automation

**Mitigation:**
- Implement human-like delays
- Rotate user agents
- Use residential proxies (optional)
- Implement CAPTCHA solving service integration (optional)

### Risk 2: CAPTCHA/MFA Cannot Be Automated

**Impact:** Medium - Requires user intervention

**Mitigation:**
- Implement pause mechanism
- Notify user immediately
- Resume after manual completion
- Take screenshot for verification

### Risk 3: Ollama Resource Requirements

**Impact:** Medium - Requires significant RAM/CPU

**Mitigation:**
- Document hardware requirements
- Provide cloud AI fallback
- Optimize prompts for smaller models

### Risk 4: AI Hallucination

**Impact:** Medium - AI may generate incorrect content

**Mitigation:**
- Validate AI outputs
- User approval before submission
- Learn from feedback (incorrect applications)

### Risk 5: Job Board UI Changes

**Impact:** Medium - Adapters break when UI changes

**Mitigation:**
- Implement robust selector strategies
- Monitor for failures
- Quick update mechanism
- Community contribution for adapter updates

---

## 31. Success Metrics

### SM-1: Agent Effectiveness

- Application success rate: >90%
- Interview rate: >20% of applications
- Time to first interview: <7 days from mission start

### SM-2: User Satisfaction

- User retention: >80% after 30 days
- User NPS: >50
- Support tickets: <5 per 100 users

### SM-3: Technical Performance

- Agent loop uptime: >99%
- API response time: p95 <2s
- Browser automation success rate: >95%

### SM-4: Adoption

- Active users: 1000+ within 6 months
- Missions created: 5000+ within 6 months
- Applications submitted: 50000+ within 6 months

---

**End of SRS v2.0**
