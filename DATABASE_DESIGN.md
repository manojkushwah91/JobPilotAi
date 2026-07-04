# JobPilot AI v2.0 — Database Design

**Version:** 2.0  
**Status:** Draft  
**Product:** JobPilot AI — "Offline-First Autonomous AI Job Agent"  
**Author:** Chief Software Architect  

---

## Table of Contents

1. Design Principles
2. Entity Relationship Diagram
3. Table Definitions
4. Index Strategy
5. Partitioning Strategy
6. Full-Text Search Schema
7. Vector Search Schema (pgvector)
8. Migration Strategy
9. Read Replica Schema
10. Optimization Strategy
11. Backup & Recovery
12. Appendix: Data Dictionary

---

## 1. Design Principles

| # | Principle | Application |
|---|-----------|-------------|
| 1 | **Domain-Driven Tables** | Each bounded context owns its tables. No cross-context FK constraints (enforced in application layer). |
| 2 | **UUID Primary Keys** | All PKs use UUID v7 (time-ordered, reduces index fragmentation). No auto-increment integers. |
| 3 | **Soft Delete** | All user-facing entities use `deleted_at` timestamp. Data is never physically deleted during normal operation. |
| 4 | **JSONB for Flexible Schemas** | Polymorphic/semi-structured data uses JSONB columns (mission config, memory metadata, task parameters). |
| 5 | **Created/Updated Timestamps** | Every table has `created_at` and `updated_at` with database defaults. |
| 6 | **Immutable Audit Trail** | Status changes, timeline events, and domain events are append-only. Never updated, only inserted. |
| 7 | **Read-Optimized Reporting** | Analytics tables are materialized views or separate read-models. Never query transactional tables for dashboards. |
| 8 | **Encryption at Rest** | PII columns use `pgcrypto` AES-256 encryption. Encryption keys stored in Vault. |
| 9 | **Pluggable Embeddings** | pgvector column (1536d) for semantic search. Indexed with IVFFlat. |
| 10 | **Partitioning by Time** | Large tables (memory, task, application) are partitioned by month. |

---

## 2. Entity Relationship Diagram

### 2.1 Core Domain ERD

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│     User     │1──N│     Mission      │1──N│       Task        │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ email (uniq) │     │ FK: user_id     │     │ FK: mission_id   │
│ password_hash│     │ name             │     │ type (enum)      │
│ role (enum)  │     │ goal             │     │ status (enum)    │
│ deleted_at   │     │ config (JSONB)   │     │ parameters (JSONB)│
└──────┬───────┘     │ status (enum)    │     │ result (JSONB)   │
       │             │ metrics (JSONB)  │     │ error_message     │
       │1:N          │ started_at       │     │ created_at       │
       │             │ completed_at     │     └──────────────────┘
┌──────▼───────┐     │ deleted_at       │
│CandidateProfile│     └──────────────────┘
│              │
│ PK: id (UUID)│     ┌──────────────────┐     ┌──────────────────┐
│ FK: user_id  │     │   JobListing     │1──N│  JobAnalysis     │
│ full_name    │     │                  │     │                  │
│ email        │     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ phone        │     │ source+v3 (uniq) │     │ FK: job_id       │
│ location     │     │ title            │     │ FK: user_id      │
│ skills (JSONB)│    │ company_name     │     │ compatibility_score│
│ experience   │     │ description      │     │ score_breakdown  │
│ (JSONB)      │     │ requirements     │     │ (JSONB)          │
│ education    │     │ (JSONB)          │     │ matched_skills   │
│ (JSONB)      │     │ salary (JSONB)   │     │ (JSONB)          │
│ deleted_at   │     │ location (JSONB) │     │ missing_skills   │
└──────────────┘     │ embeddings(1536)│     │ (JSONB)          │
                     │ posted_at        │     │ scam_detection   │
                     │ scraped_at       │     │ (JSONB)          │
                     │ deleted_at       │     │ analyzed_at      │
                     └──────────────────┘     └──────────────────┘
                               │
                               │N:1
                               │
                     ┌─────────▼──────────┐
                     │   Application      │
                     │                   │
                     │ PK: id (UUID)     │
                     │ FK: user_id       │
                     │ FK: job_id        │
                     │ FK: resume_id     │
                     │ FK: cover_letter_id│
                     │ status (enum)     │
                     │ automation_result │
                     │ (JSONB)           │
                     │ screenshot_url    │
                     │ submitted_at      │
                     │ deleted_at        │
                     └───────────────────┘
```

### 2.2 Agent Runtime ERD

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  AgentState  │1──N│     Memory       │1──N│     Episode       │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ FK: user_id  │     │ FK: user_id      │     │ FK: user_id      │
│ FK: mission_id│    │ type (enum)      │     │ FK: mission_id   │
│ status (enum)│    │ key              │     │ description      │
│ phase (enum) │    │ value (TEXT)      │     │ memories (JSONB)  │
│ current_task │    │ metadata (JSONB)  │     │ outcome (enum)    │
│ (JSONB)      │    │ confidence       │     │ lessons_learned  │
│ context      │    │ last_accessed    │     │ (TEXT)           │
│ (JSONB)      │    │ created_at       │     │ created_at       │
│ loop_iteration│   └──────────────────┘     └──────────────────┘
│ created_at   │
│ updated_at   │
└──────────────┘
```

---

## 3. Table Definitions

### 3.1 User Table (KEEP)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
```

### 3.2 Mission Table (NEW)

```sql
CREATE TABLE missions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    goal TEXT NOT NULL,
    target_salary JSONB,
    locations JSONB NOT NULL DEFAULT '[]',
    experience_min INTEGER,
    experience_max INTEGER,
    preferred_roles JSONB NOT NULL DEFAULT '[]',
    preferred_companies JSONB NOT NULL DEFAULT '[]',
    avoid_companies JSONB NOT NULL DEFAULT '[]',
    daily_apply_limit INTEGER NOT NULL DEFAULT 20,
    deadline DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    metrics JSONB NOT NULL DEFAULT '{"jobsFound": 0, "jobsAnalyzed": 0, "applicationsSubmitted": 0, "interviewsScheduled": 0, "offersReceived": 0}',
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_mission_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_missions_user_id ON missions(user_id);
CREATE INDEX idx_missions_status ON missions(status);
CREATE INDEX idx_missions_deleted_at ON missions(deleted_at) WHERE deleted_at IS NULL;
```

### 3.3 Candidate Profile Table (NEW)

```sql
CREATE TABLE candidate_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    location VARCHAR(255),
    skills JSONB NOT NULL DEFAULT '[]',
    experience JSONB NOT NULL DEFAULT '[]',
    education JSONB NOT NULL DEFAULT '[]',
    certifications JSONB NOT NULL DEFAULT '[]',
    summary TEXT,
    current_salary JSONB,
    expected_salary JSONB,
    preferences JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_candidate_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_candidate_profiles_user_id ON candidate_profiles(user_id);
CREATE INDEX idx_candidate_profiles_deleted_at ON candidate_profiles(deleted_at) WHERE deleted_at IS NULL;
```

### 3.4 Job Listing Table (REFACTOR)

```sql
CREATE TABLE job_listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source VARCHAR(100) NOT NULL,
    source_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    description TEXT,
    requirements JSONB,
    salary JSONB,
    location JSONB,
    employment_type VARCHAR(50),
    experience_level VARCHAR(50),
    posted_at TIMESTAMP WITH TIME ZONE,
    scraped_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    embeddings vector(1536),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_job_source UNIQUE (source, source_id)
);

CREATE INDEX idx_job_listings_source ON job_listings(source);
CREATE INDEX idx_job_listings_source_id ON job_listings(source_id);
CREATE INDEX idx_job_listings_company ON job_listings(company_name);
CREATE INDEX idx_job_listings_posted_at ON job_listings(posted_at DESC);
CREATE INDEX idx_job_listings_is_active ON job_listings(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_job_listings_deleted_at ON job_listings(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_listings_embeddings ON job_listings USING ivfflat (embeddings vector_cosine_ops);
```

### 3.5 Job Analysis Table (NEW)

```sql
CREATE TABLE job_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL,
    user_id UUID NOT NULL,
    compatibility_score INTEGER NOT NULL CHECK (compatibility_score BETWEEN 0 AND 100),
    score_breakdown JSONB,
    matched_skills JSONB NOT NULL DEFAULT '[]',
    missing_skills JSONB NOT NULL DEFAULT '[]',
    scam_detection JSONB,
    interview_probability DECIMAL(5,2),
    analyzed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_job FOREIGN KEY (job_id) REFERENCES job_listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_analysis_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_job_analyses_job_id ON job_analyses(job_id);
CREATE INDEX idx_job_analyses_user_id ON job_analyses(user_id);
CREATE INDEX idx_job_analyses_compatibility ON job_analyses(compatibility_score DESC);
```

### 3.6 Application Table (REFACTOR - Read-Only)

```sql
CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    job_id UUID NOT NULL,
    resume_id UUID,
    cover_letter_id UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    automation_result JSONB,
    screenshot_url VARCHAR(500),
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES job_listings(id) ON DELETE CASCADE
);

CREATE INDEX idx_applications_user_id ON applications(user_id);
CREATE INDEX idx_applications_job_id ON applications(job_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_submitted_at ON applications(submitted_at DESC);
CREATE INDEX idx_applications_deleted_at ON applications(deleted_at) WHERE deleted_at IS NULL;
```

### 3.7 Memory Table (NEW)

```sql
CREATE TABLE memory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    key VARCHAR(255) NOT NULL,
    value TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}',
    confidence DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    last_accessed TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_memory_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_memory_user_id ON memory(user_id);
CREATE INDEX idx_memory_type ON memory(type);
CREATE INDEX idx_memory_key ON memory(key);
CREATE INDEX idx_memory_confidence ON memory(confidence);
CREATE INDEX idx_memory_last_accessed ON memory(last_accessed DESC);

-- Monthly partitions
CREATE TABLE memory_2024_01 PARTITION OF memory FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
CREATE TABLE memory_2024_02 PARTITION OF memory FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
-- ... add more partitions as needed
```

### 3.8 Episode Table (NEW)

```sql
CREATE TABLE episodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    mission_id UUID,
    description TEXT NOT NULL,
    memories JSONB NOT NULL DEFAULT '[]',
    outcome VARCHAR(50),
    lessons_learned TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_episode_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_episode_mission FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE SET NULL
);

CREATE INDEX idx_episodes_user_id ON episodes(user_id);
CREATE INDEX idx_episodes_mission_id ON episodes(mission_id);
CREATE INDEX idx_episodes_created_at ON episodes(created_at DESC);
```

### 3.9 Task Table (NEW)

```sql
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority INTEGER NOT NULL DEFAULT 5,
    parameters JSONB NOT NULL DEFAULT '{}',
    result JSONB,
    error_message TEXT,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_mission FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_tasks_mission_id ON tasks(mission_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority DESC);
CREATE INDEX idx_tasks_scheduled_at ON tasks(scheduled_at);

-- Monthly partitions
CREATE TABLE tasks_2024_01 PARTITION OF tasks FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
CREATE TABLE tasks_2024_02 PARTITION OF tasks FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
-- ... add more partitions as needed
```

### 3.10 Agent State Table (NEW)

```sql
CREATE TABLE agent_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    mission_id UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'IDLE',
   phase VARCHAR(50) NOT NULL DEFAULT 'OBSERVE',
    current_task_id UUID,
    loop_iteration INTEGER NOT NULL DEFAULT 0,
    context JSONB NOT NULL DEFAULT '{}',
    recent_thoughts JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_state_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_state_mission FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE SET NULL,
    CONSTRAINT fk_agent_state_task FOREIGN KEY (current_task_id) REFERENCES tasks(id) ON DELETE SET NULL
);

CREATE INDEX idx_agent_states_user_id ON agent_states(user_id);
CREATE INDEX idx_agent_states_status ON agent_states(status);
```

### 3.11 Resumes Table (KEEP)

```sql
CREATE TABLE resumes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INTEGER NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    parsed_content JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_resumes_deleted_at ON resumes(deleted_at) WHERE deleted_at IS NULL;
```

### 3.12 Cover Letters Table (KEEP)

```sql
CREATE TABLE cover_letters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    job_id UUID,
    title VARCHAR(255),
    body TEXT NOT NULL,
    tone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_cover_letter_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cover_letter_job FOREIGN KEY (job_id) REFERENCES job_listings(id) ON DELETE SET NULL
);

CREATE INDEX idx_cover_letters_user_id ON cover_letters(user_id);
CREATE INDEX idx_cover_letters_job_id ON cover_letters(job_id);
CREATE INDEX idx_cover_letters_deleted_at ON cover_letters(deleted_at) WHERE deleted_at IS NULL;
```

### 3.13 Notifications Table (KEEP - SIMPLIFIED)

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL DEFAULT 'IN_APP',
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read_at ON notifications(read_at) WHERE read_at IS NULL;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
```

---

## 4. Index Strategy

### 4.1 Primary Key Indexes

All tables use UUID v7 primary keys, which are time-ordered and reduce index fragmentation.

### 4.2 Foreign Key Indexes

All foreign key columns are indexed for join performance.

### 4.3 Query-Specific Indexes

- `missions.status` - For filtering active missions
- `job_listings.source` - For filtering by job board
- `job_listings.is_active` - For filtering active jobs
- `job_listings.embeddings` - Vector similarity search (IVFFlat)
- `applications.status` - For filtering by application status
- `memory.type` - For filtering by memory type
- `tasks.status` - For filtering by task status
- `tasks.priority` - For task queue ordering

### 4.4 Partial Indexes

Partial indexes are used for soft-delete filtering:
- `*_deleted_at` - Index only non-deleted records
- `job_listings.is_active` - Index only active jobs
- `notifications.read_at` - Index only unread notifications

---

## 5. Partitioning Strategy

### 5.1 Partitioned Tables

Large tables are partitioned by month to improve query performance and simplify maintenance:

- `memory` - Partitioned by `created_at`
- `tasks` - Partitioned by `created_at`

### 5.2 Partition Maintenance

Partitions are created automatically using pg_partman or manually via migration scripts.

---

## 6. Full-Text Search Schema

### 6.1 Job Search Full-Text Index

```sql
ALTER TABLE job_listings ADD COLUMN search_vector tsvector;

CREATE INDEX idx_job_listings_search ON job_listings USING GIN (search_vector);

CREATE TRIGGER job_listings_search_vector_update
BEFORE INSERT OR UPDATE ON job_listings
FOR EACH ROW EXECUTE FUNCTION
tsvector_update_trigger(search_vector, 'pg_catalog.simple', title, company_name, description);
```

### 6.2 Search Query Example

```sql
SELECT * FROM job_listings
WHERE search_vector @@ to_tsquery('java & spring & boot')
ORDER BY ts_rank(search_vector, to_tsquery('java & spring & boot')) DESC;
```

---

## 7. Vector Search Schema (pgvector)

### 7.1 Enable pgvector Extension

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 7.2 Embeddings Column

Job listings have an `embeddings` column of type `vector(1536)` for semantic search.

### 7.3 Vector Index

```sql
CREATE INDEX idx_job_listings_embeddings ON job_listings USING ivfflat (embeddings vector_cosine_ops)
WITH (lists = 100);
```

### 7.4 Vector Search Query Example

```sql
SELECT * FROM job_listings
ORDER BY embeddings <-> '[0.1, 0.2, ...]'::vector
LIMIT 10;
```

---

## 8. Migration Strategy

### 8.1 Flyway Migrations

All schema changes are managed via Flyway migrations in `backend/jobpilot-infrastructure/src/main/resources/db/migration/`.

### 8.2 Migration Naming Convention

`V{version}__{description}.sql`

Example: `V1__core_tables.sql`, `V2__mission_tables.sql`

### 8.3 Rollback Strategy

Flyway does not support rollback by default. For production, use:
- Database backups before migrations
- Test migrations in staging first
- Use `repeatable` migrations for idempotent changes

---

## 9. Read Replica Schema

### 9.1 Read-Only Queries

Read replicas are used for:
- Job search queries
- Application tracking queries
- Analytics queries

### 9.2 Replication Setup

PostgreSQL streaming replication with:
- 1 primary (write)
- 1-2 replicas (read)

---

## 10. Optimization Strategy

### 10.1 Connection Pooling

Use HikariCP with:
- Maximum pool size: 20
- Minimum idle: 5
- Connection timeout: 30s

### 10.2 Query Optimization

- Use EXPLAIN ANALYZE for slow queries
- Add appropriate indexes
- Use prepared statements
- Avoid SELECT *

### 10.3 Caching Strategy

- Redis for frequently accessed data (agent state, task queue)
- Application-level caching for job listings (TTL: 1 hour)

---

## 11. Backup & Recovery

### 11.1 Backup Strategy

- Daily full backups (pg_dump)
- Hourly WAL archiving
- Retention: 30 days

### 11.2 Recovery Strategy

- Point-in-time recovery (PITR) using WAL
- Restore from latest backup + WAL replay

---

## 12. Appendix: Data Dictionary

### 12.1 Mission Status Enum

| Value | Description |
|-------|-------------|
| DRAFT | Mission created but not started |
| ACTIVE | Mission is running |
| PAUSED | Mission is paused by user |
| COMPLETED | Mission completed successfully |
| CANCELLED | Mission cancelled by user |

### 12.2 Agent Status Enum

| Value | Description |
|-------|-------------|
| IDLE | Agent is not running |
| RUNNING | Agent is running |
| PAUSED | Agent is paused |
| STOPPED | Agent is stopped |
| ERROR | Agent encountered error |

### 12.3 Agent Phase Enum

| Value | Description |
|-------|-------------|
| OBSERVE | Agent is observing state |
| THINK | Agent is reasoning |
| PLAN | Agent is planning tasks |
| EXECUTE | Agent is executing tasks |
| VERIFY | Agent is verifying results |
| LEARN | Agent is learning from results |

### 12.4 Task Type Enum

| Value | Description |
|-------|-------------|
| DISCOVER_JOBS | Discover jobs from job boards |
| ANALYZE_JOB | Analyze job description |
| TAILOR_RESUME | Tailor resume for job |
| GENERATE_COVER_LETTER | Generate cover letter |
| FILL_APPLICATION | Fill application form |
| SUBMIT_APPLICATION | Submit application |
| VERIFY_SUBMISSION | Verify submission |

### 12.5 Task Status Enum

| Value | Description |
|-------|-------------|
| PENDING | Task is pending |
| QUEUED | Task is queued |
| RUNNING | Task is running |
| COMPLETED | Task completed successfully |
| FAILED | Task failed |
| CANCELLED | Task cancelled |

### 12.6 Memory Type Enum

| Value | Description |
|-------|-------------|
| PREFERENCE | User preference (e.g., "Never apply to TCS") |
| OBSERVATION | Observation (e.g., "Applied to Adobe last week") |
| OUTCOME | Outcome (e.g., "Rejected by Microsoft") |
| STRATEGY | Strategy (e.g., "LinkedIn Easy Apply works best on Tuesdays") |
| KNOWLEDGE | Knowledge (e.g., "Greenhouse ATS requires cover letter") |

---

**End of Database Design v2.0**
