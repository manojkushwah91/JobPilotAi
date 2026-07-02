# JobPilot AI — Database Design

**Version:** 1.0  
**Status:** Draft  
**Phase:** 4 of 35  
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
| 4 | **JSONB for Flexible Schemas** | Polymorphic/semi-structured data uses JSONB columns (resume sections, form fields, preferences). |
| 5 | **Created/Updated Timestamps** | Every table has `created_at` and `updated_at` with database defaults. |
| 6 | **Immutable Audit Trail** | Status changes, timeline events, and domain events are append-only. Never updated, only inserted. |
| 7 | **Read-Optimized Reporting** | Analytics tables are materialized views or separate read-models. Never query transactional tables for dashboards. |
| 8 | **Encryption at Rest** | PII columns use `pgcrypto` AES-256 encryption. Encryption keys stored in Vault. |
| 9 | **Pluggable Embeddings** | pgvector column (1536d) for semantic search. Indexed with IVFFlat. |
| 10 | **Partitioning by Time** | Large tables (audit_log, notification, job_listing) are partitioned by month. |

---

## 2. Entity Relationship Diagram

### 2.1 Core Domain ERD

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│     User     │1──N│     Resume       │1──N│   CoverLetter    │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ email (uniq) │     │ FK: user_id     │     │ FK: user_id      │
│ password_hash│     │ FK: template_id │     │ FK: resume_id?   │
│ role (enum)  │     │ sections (JSONB)│     │ FK: job_listing? │
│ email_verif  │     │ versions (JSONB)│     │ body (JSONB)     │
│ deleted_at   │     │ deleted_at      │     │ tone (enum)      │
└──────┬───────┘     └──────────────────┘     └──────────────────┘
       │
       │1:N
       │
┌──────▼───────┐     ┌──────────────────┐     ┌──────────────────┐
│ Application  │N──1│   JobListing     │     │  CompanyProfile  │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ FK: user_id  │     │ source+v3 (uniq) │     │ name (uniq)      │
│ FK: job_id   │     │ title           │     │ description      │
│ FK: resume_id│     │ company_name    │     │ tech_stack(JSONB)│
│ status (enum)│     │ description     │     │ salary_data(JSONB)│
│ autom. status│     │ salary (JSONB)  │     │ interview_notes  │
│ salary_offer │     │ location (JSONB)│     │ (JSONB)          │
│ applied_at   │     │ embeddings(1536)│     │ linkedin_url     │
│ created_at   │     │ posted_at       │     │ updated_at       │
└──────┬───────┘     │ scraped_at      │     └──────────────────┘
       │             └──────────────────┘
       │1:N                    │
       │                       │N:N (through job_skills)
┌──────▼───────┐              │
│  Timeline    │     ┌──────────────────┐
│  Event       │     │   SavedSearch   │
│              │     │                 │
│ PK: id (UUID)│     │ PK: id (UUID)   │
│ FK: app_id   │     │ FK: user_id     │
│ type (enum)  │     │ name            │
│ title        │     │ query (JSONB)   │
│ description  │     │ notif_enabled   │
│ metadata     │     │ last_notified   │
│  (JSONB)     │     │ created_at      │
│ timestamp    │     └──────────────────┘
└──────────────┘
```

### 2.2 AI & Automation ERD

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│AutomationSess│1──N│   PromptTemplate │     │   AtsAnalysis   │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ FK: app_id   │     │ use_case (enum)  │     │ FK: resume_id   │
│ FK: user_id  │     │ name             │     │ FK: job_listing? │
│ state (enum) │     │ version          │     │ score (int)      │
│ job_url      │     │ system_prompt    │     │ keyword_matches  │
│ form_fields  │     │ user_template    │     │  (JSONB)         │
│  (JSONB)     │     │ variables (JSONB)│     │ missing_kw (JSONB)│
│ submitted    │     │ model            │     │ suggestions      │
│  data(JSONB) │     │ temperature      │     │  (JSONB)         │
│ evidence     │     │ max_tokens       │     │ analyzed_at      │
│  (JSONB)     │     │ is_active        │     └──────────────────┘
│ error_msg    │     │ created_at       │
│ attempt_ct   │     └──────────────────┘
│ proxy_used   │
│ started_at   │     ┌──────────────────┐
│ completed_at │     │ AiUsageLog      │
└──────┬───────┘     │                  │
       │             │ PK: id (UUID)    │
       │             │ FK: user_id      │
┌──────▼───────┐     │ use_case (enum)  │
│InterviewSess │     │ provider (enum)  │
│              │     │ model            │
│ PK: id (UUID)│     │ prompt_tokens    │
│ FK: user_id  │     │ completion_tok   │
│ target_role  │     │ total_tokens     │
│ target_comp  │     │ cost_micro_usd   │
│ mode (enum)  │     │ latency_ms       │
│ status (enum)│     │ created_at       │
│ questions    │     └──────────────────┘
│  (JSONB)     │
│ responses    │     ┌──────────────────┐
│  (JSONB)     │     │ Outbox           │
│ overall_score│     │                  │
│ feedback     │     │ PK: id (UUID)    │
│  (JSONB)     │     │ aggregate_type   │
│ duration_sec │     │ aggregate_id     │
│ started_at   │     │ event_type       │
│ completed_at │     │ event_payload    │
└──────────────┘     │  (JSONB)         │
                     │ status (enum)    │
                     │ retry_count      │
                     │ created_at       │
                     │ published_at     │
                     └──────────────────┘
```

### 2.3 Billing & Notifications ERD

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ Subscription │     │   Notification   │     │  AuditLog        │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: id (UUID)    │
│ FK: user_id  │     │ FK: user_id      │     │ FK: user_id?     │
│ plan (enum)  │     │ type (enum)      │     │ action (varchar) │
│ stripe_cust  │     │ title            │     │ resource_type    │
│ stripe_sub   │     │ body             │     │ resource_id      │
│ status (enum)│     │ data (JSONB)     │     │ old_value(JSONB) │
│ period_start │     │ channel (text[]) │     │ new_value(JSONB) │
│ period_end   │     │ status (enum)    │     │ ip_address       │
│ canceled_at  │     │ read_at          │     │ user_agent       │
│ created_at   │     │ created_at       │     │ created_at       │
│ updated_at   │     │ PARTITION BY     │     │ PARTITION BY     │
└──────────────┘     │  month(created)  │     │  month(created)  │
                     └──────────────────┘     └──────────────────┘

┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ UserSettings │     │ FeatureFlag      │     │ SavedJob         │
│              │     │                  │     │                  │
│ PK: id (UUID)│     │ PK: id (UUID)    │     │ PK: user_id      │
│ FK: user_id  │     │ key (uniq)       │     │ PK: job_listing  │
│ theme (enum) │     │ enabled (bool)   │     │     _id          │
│ language     │     │ description      │     │ saved_at         │
│ timezone     │     │ created_at       │     │ notes            │
│ ai_pref      │     │ updated_at       │     │  (optional)      │
│  (JSONB)     │     └──────────────────┘     └──────────────────┘
│ privacy      │
│  (JSONB)     │
│ display      │
│  (JSONB)     │
└──────────────┘
```

---

## 3. Table Definitions

### 3.1 `users` — Identity & Access

```sql
-- Design Notes:
-- - Core identity table for all users
-- - Soft delete (deleted_at) for GDPR compliance
-- - email is the primary login identifier
-- - oauth_providers is JSONB for flexible provider support
-- - role controls feature access via RBAC

-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
email               VARCHAR(320) NOT NULL,
password_hash       VARCHAR(255),
role                VARCHAR(20) NOT NULL DEFAULT 'FREE_USER',
                    -- CHECK: FREE_USER, PRO_USER, ENTERPRISE_USER, ADMIN
email_verified_at   TIMESTAMPTZ,
email_verify_token  VARCHAR(255),        -- hashed token for verification
email_verify_sent   TIMESTAMPTZ,
password_reset_token VARCHAR(255),       -- hashed
password_reset_sent TIMESTAMPTZ,
oauth_providers     JSONB DEFAULT '[]'::jsonb,
                    -- [{"provider": "GOOGLE", "provider_user_id": "xxx", "email": "..."}]
failed_login_attempts INTEGER DEFAULT 0,
locked_until        TIMESTAMPTZ,
last_login_at       TIMESTAMPTZ,
last_login_ip       INET,
deleted_at          TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Constraints:
UNIQUE (email) WHERE deleted_at IS NULL

-- Indexes:
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role ON users(role) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_created ON users(created_at);
CREATE INDEX idx_users_oauth ON users USING gin(oauth_providers);
```

### 3.2 `user_profiles` — Extended Profile

```sql
-- Design Notes:
-- - 1:1 with users table (separated to keep users table lean)
-- - PII fields (phone) encrypted with pgcrypto
-- - experiences and education stored as JSONB arrays for flexibility

-- Columns:
id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id                 UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
full_name               VARCHAR(150) NOT NULL,
headline                VARCHAR(300),
phone_encrypted         BYTEA,                 -- pgcrypto encrypt
location                JSONB,                 -- {"city": "...", "state": "...", "country": "...", "remote": true}
work_authorization      JSONB,                 -- {"citizenship": "US", "visa_type": null, "requires_sponsorship": false}
social_links            JSONB,                 -- {"linkedin": "...", "github": "...", "portfolio": "...", "twitter": "..."}
skills                  JSONB DEFAULT '[]'::jsonb,
                    -- [{"name": "Java", "proficiency": "EXPERT", "years": 8}, ...]
experiences             JSONB DEFAULT '[]'::jsonb,
                    -- [{"title": "Sr Eng", "company": "Co", "start": "2020-01", "end": null,
                    --   "current": true, "description": "...", "technologies": ["Java","AWS"]}, ...]
education               JSONB DEFAULT '[]'::jsonb,
                    -- [{"degree": "BS", "institution": "MIT", "field": "CS", "start": "2014", "end": "2018", "gpa": "3.8"}]
preferences             JSONB DEFAULT '{}'::jsonb,
                    -- {"theme": "DARK", "language": "en", "timezone": "America/New_York",
                    --  "email_frequency": "DAILY", "ai_provider": "OPENAI"}
avatar_url              VARCHAR(1024),
created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_skills ON user_profiles USING gin(skills);
CREATE INDEX idx_user_profiles_name ON user_profiles USING gin(to_tsvector('english', full_name));
```

### 3.3 `resumes` — Resume Studio

```sql
-- Design Notes:
-- - Aggregate root for all resume operations
-- - sections stored as JSONB (polymorphic per section type)
-- - versions stored as JSONB array (immutable, append-only)
-- - ats_scores stored as JSONB array (tracking history)
-- - template_id references an internal or external template

-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
title               VARCHAR(200) NOT NULL,
template_id         VARCHAR(50) DEFAULT 'modern',
sections            JSONB NOT NULL DEFAULT '[]'::jsonb,
                    -- [{"id": "...", "type": "EXPERIENCE", "title": "Experience",
                    --   "content": {"items": [...]}, "order": 1}, ...]
versions            JSONB NOT NULL DEFAULT '[]'::jsonb,
                    -- [{"version_number": 1, "label": "Original", "content_snapshot": {...},
                    --   "created_at": "...", "is_active": true}, ...]
current_version     INTEGER NOT NULL DEFAULT 1,
ats_scores          JSONB DEFAULT '[]'::jsonb,
                    -- [{"score": 85, "job_hash": "...", "missing_keywords": [...], "analyzed_at": "..."}, ...]
file_url            VARCHAR(1024),           -- uploaded original file (PDF/DOCX)
file_type           VARCHAR(10),             -- pdf, docx, txt
word_count          INTEGER,
deleted_at          TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_resumes_user_id ON resumes(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_resumes_created ON resumes(user_id, created_at DESC);
```

### 3.4 `cover_letters` — Cover Letter Engine

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
resume_id           UUID REFERENCES resumes(id) ON DELETE SET NULL,
job_listing_id      UUID REFERENCES job_listings(id) ON DELETE SET NULL,
title               VARCHAR(200) NOT NULL,
recipient_name      VARCHAR(150),
recipient_title     VARCHAR(200),
company_name        VARCHAR(200) NOT NULL,
body                JSONB NOT NULL,
                    -- {"salutation": "...", "opening": "...", "body_paragraphs": [...],
                    --  "closing": "...", "signature": "..."}
tone                VARCHAR(20) NOT NULL DEFAULT 'PROFESSIONAL',
                    -- CHECK: PROFESSIONAL, ENTHUSIASTIC, CONFIDENT, FORMAL, WARM
word_count          INTEGER NOT NULL DEFAULT 0,
version             INTEGER NOT NULL DEFAULT 1,
file_url            VARCHAR(1024),
deleted_at          TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_cover_letters_user_id ON cover_letters(user_id) WHERE deleted_at IS NULL;
```

### 3.5 `job_listings` — Job Discovery

```sql
-- Design Notes:
-- - Core table for aggregated job listings
-- - (source, source_id) unique for deduplication
-- - tsvector column for full-text search (updated via trigger)
-- - embeddings column (1536d) for semantic search via pgvector
-- - Partitioned by month on scraped_at for older data management

-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
source              VARCHAR(50) NOT NULL,
                    -- CHECK: LINKEDIN, INDEED, GLASSDOOR, GOOGLE_JOBS, COMPANY, MANUAL
source_id           VARCHAR(255) NOT NULL,    -- source's internal ID
title               VARCHAR(300) NOT NULL,
company_name        VARCHAR(200) NOT NULL,
company_logo_url    VARCHAR(1024),
company_id          UUID REFERENCES company_profiles(id) ON DELETE SET NULL,
location            JSONB,                    -- {"city": "...", "state": "...", "country": "...", "remote": "REMOTE"}
salary_min          NUMERIC(12,2),
salary_max          NUMERIC(12,2),
salary_currency     VARCHAR(3) DEFAULT 'USD',
salary_period       VARCHAR(10) DEFAULT 'YEARLY',
description         TEXT,                     -- stripped HTML
requirements        JSONB DEFAULT '[]'::jsonb,
responsibilities    JSONB DEFAULT '[]'::jsonb,
benefits            JSONB DEFAULT '[]'::jsonb,
employment_type     VARCHAR(20),
                    -- FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, TEMPORARY
experience_level    VARCHAR(20),
                    -- ENTRY, MID, SENIOR, LEAD, EXECUTIVE
industry            VARCHAR(100),
skills              JSONB DEFAULT '[]'::jsonb, -- extracted skills list
application_url     VARCHAR(2048) NOT NULL,
application_deadline TIMESTAMPTZ,
posted_at           TIMESTAMPTZ,
scraped_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
embeddings          vector(1536),             -- pgvector
is_active           BOOLEAN NOT NULL DEFAULT TRUE,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Constraints:
UNIQUE (source, source_id)

-- Indexes:
CREATE UNIQUE INDEX idx_job_listings_source ON job_listings(source, source_id);
CREATE INDEX idx_job_listings_company ON job_listings(company_name);
CREATE INDEX idx_job_listings_posted ON job_listings(posted_at DESC);
CREATE INDEX idx_job_listings_active ON job_listings(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_job_listings_fts ON job_listings USING gin(to_tsvector('english', title || ' ' || company_name || ' ' || coalesce(description, '')));
CREATE INDEX idx_job_listings_embeddings ON job_listings USING ivfflat (embeddings vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_job_listings_scraped ON job_listings(scraped_at);
```

### 3.6 `job_skills` — Job- Skill Association

```sql
-- Design Notes:
-- - Normalized many-to-many between jobs and skills
-- - Enables efficient skill-based filtering and matching

-- Columns:
job_listing_id      UUID NOT NULL REFERENCES job_listings(id) ON DELETE CASCADE,
skill_name          VARCHAR(100) NOT NULL,
is_required         BOOLEAN DEFAULT TRUE,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Constraints:
PRIMARY KEY (job_listing_id, skill_name)

-- Indexes:
CREATE INDEX idx_job_skills_skill ON job_skills(skill_name);
```

### 3.7 `saved_searches` — Saved Search Alerts

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
name                VARCHAR(200) NOT NULL,
query               JSONB NOT NULL,
                    -- {"keywords": "java remote", "location": "New York", "remote": "REMOTE",
                    --  "salary_min": 100000, "employment_types": ["FULL_TIME"],
                    --  "experience_levels": ["SENIOR"], "posted_within_days": 7}
notifications_enabled BOOLEAN DEFAULT TRUE,
notify_frequency    VARCHAR(10) DEFAULT 'INSTANT',
                    -- INSTANT, DAILY, WEEKLY
last_notified_at    TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Constraints:
UNIQUE (user_id, name)

-- Indexes:
CREATE INDEX idx_saved_searches_user ON saved_searches(user_id);
```

### 3.8 `saved_jobs` — Bookmarked Jobs

```sql
-- Columns:
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
job_listing_id      UUID NOT NULL REFERENCES job_listings(id) ON DELETE CASCADE,
notes               TEXT,
saved_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Constraints:
PRIMARY KEY (user_id, job_listing_id)

-- Indexes:
CREATE INDEX idx_saved_jobs_user ON saved_jobs(user_id, saved_at DESC);
```

### 3.9 `applications` — Application Tracker (ATS)

```sql
-- Design Notes:
-- - Aggregate root for the ATS pipeline
-- - status_history is append-only JSONB array (immutable audit trail)
-- - automation_info tracks browser automation state
-- - salary_offered captured when status moves to OFFER

-- Columns:
id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
job_listing_id          UUID NOT NULL REFERENCES job_listings(id) ON DELETE CASCADE,
resume_id               UUID REFERENCES resumes(id) ON DELETE SET NULL,
cover_letter_id         UUID REFERENCES cover_letters(id) ON DELETE SET NULL,
status                  VARCHAR(20) NOT NULL DEFAULT 'SAVED',
                        -- CHECK: SAVED, APPLIED, PHONE_SCREEN, TECHNICAL_INTERVIEW,
                        --        ONSITE_INTERVIEW, OFFER, ACCEPTED, REJECTED, WITHDRAWN
status_history          JSONB DEFAULT '[]'::jsonb,
                        -- [{"from": "SAVED", "to": "APPLIED", "changed_by": "USER", "timestamp": "...", "note": "..."}, ...]
automation_status       VARCHAR(20),
                        -- CHECK: PENDING, IN_PROGRESS, SUBMITTED, CAPTCHA, FAILED, MANUAL_APPROVAL (nullable)
automation_session_id   UUID,                -- FK to automation_sessions (app-level)
automation_evidence_url VARCHAR(1024),
automation_error        TEXT,
applied_at              TIMESTAMPTZ,
salary_offered_min      NUMERIC(12,2),
salary_offered_max      NUMERIC(12,2),
salary_offered_currency VARCHAR(3),
deleted_at              TIMESTAMPTZ,
created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_applications_user_status ON applications(user_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_applications_job ON applications(job_listing_id);
CREATE INDEX idx_applications_automation ON applications(automation_status) WHERE automation_status IS NOT NULL;
CREATE INDEX idx_applications_created ON applications(user_id, created_at DESC);
```

### 3.10 `application_notes` — Notes per Application

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
application_id      UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
content             TEXT NOT NULL,
category            VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
                    -- GENERAL, PREP, FOLLOW_UP, RESEARCH, OFFER
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_app_notes_application ON application_notes(application_id, created_at DESC);
```

### 3.11 `application_attachments` — Files per Application

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
application_id      UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
file_name           VARCHAR(255) NOT NULL,
file_url            VARCHAR(2048) NOT NULL,   -- S3 URL
content_type        VARCHAR(100),
file_size_bytes     INTEGER,
uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_app_attachments_application ON application_attachments(application_id);
```

### 3.12 `timeline_events` — Application Timeline

```sql
-- Design Notes:
-- - Append-only log of all events for an application
-- - Immutable — never updated, only inserted
-- - Supports full timeline view for users

-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
application_id      UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
event_type          VARCHAR(40) NOT NULL,
                    -- APPLICATION_SUBMITTED, STATUS_CHANGED, NOTE_ADDED,
                    -- INTERVIEW_SCHEDULED, FOLLOW_UP_SET, OFFER_RECEIVED,
                    -- AUTOMATION_STARTED, AUTOMATION_COMPLETED, EMAIL_RECEIVED
title               VARCHAR(300) NOT NULL,
description         TEXT,
metadata            JSONB DEFAULT '{}'::jsonb,
                    -- {"interview_date": "2026-07-15T14:00:00Z", "interviewer": "John", "duration_min": 60}
event_timestamp     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_timeline_application ON timeline_events(application_id, event_timestamp);
CREATE INDEX idx_timeline_user ON timeline_events(user_id, event_timestamp DESC);
```

### 3.13 `company_profiles` — Company Intelligence

```sql
-- Columns:
id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
name                    VARCHAR(200) NOT NULL UNIQUE,
description             TEXT,
website                 VARCHAR(1024),
logo_url                VARCHAR(1024),
industry                VARCHAR(100),
headquarters            JSONB,                -- {"city": "...", "state": "...", "country": "..."}
founded_year            SMALLINT,
company_size_min        INTEGER,
company_size_max        INTEGER,
stock_symbol            VARCHAR(10),
funding_rounds          JSONB DEFAULT '[]'::jsonb,
                        -- [{"date": "...", "amount": 10000000, "round": "Series A", "investors": [...], ...}]
technology_stack        JSONB DEFAULT '[]'::jsonb,
                        -- ["React", "Python", "AWS", "Kubernetes", ...]
culture_keywords        JSONB DEFAULT '[]'::jsonb,
benefits                JSONB DEFAULT '[]'::jsonb,
interview_notes         JSONB DEFAULT '[]'::jsonb,
                        -- [{"role": "Sr Eng", "difficulty": 4, "rounds": 3, "topics": [...], "tips": "...", ...}]
salary_data             JSONB DEFAULT '[]'::jsonb,
                        -- [{"role": "Software Engineer", "min": 120000, "max": 180000, "currency": "USD", "source": "levels_fyi"}, ...]
hiring_trends           JSONB DEFAULT '{}'::jsonb,
                        -- {"open_roles": 45, "growth_rate": 12.5, "recent_hires": [...]}
glassdoor_rating        DECIMAL(2,1),
glassdoor_url           VARCHAR(1024),
linkedin_url            VARCHAR(1024),
crunchbase_url          VARCHAR(1024),
last_enriched_at        TIMESTAMPTZ,
created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_company_profiles_name ON company_profiles(name);
CREATE INDEX idx_company_profiles_industry ON company_profiles(industry);
CREATE INDEX idx_company_profiles_tech ON company_profiles USING gin(technology_stack);
```

### 3.14 `automation_sessions` — Browser Automation

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
application_id      UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
state               VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
                    -- QUEUED, INITIALIZING, NAVIGATING, FORM_DETECT, FORM_FILL,
                    -- SUBMIT, VERIFY, COMPLETED, BLOCKED, MANUAL_REQUIRED, RETRYING, FAILED
job_url             VARCHAR(2048) NOT NULL,
ats_platform        VARCHAR(30),              -- detected: GREENHOUSE, LEVER, WORKDAY, etc.
form_fields         JSONB DEFAULT '[]'::jsonb,
                    -- [{"field_name": "name", "field_type": "TEXT", "selector": "#name",
                    --   "is_required": true, "detected_label": "Full Name", "mapped_field": "full_name"}, ...]
submitted_data      JSONB DEFAULT '{}'::jsonb,
                    -- {"field_name": "value", ...}
attempt_count       INTEGER DEFAULT 0,
max_retries         INTEGER DEFAULT 3,
proxy_used          VARCHAR(100),
user_agent_used     VARCHAR(500),
evidence            JSONB DEFAULT '{}'::jsonb,
                    -- {"pre_submit_screenshot": "...", "post_submit_screenshot": "...",
                    --  "confirmation_text": "...", "page_title": "...", "logs": [...]}
error_message       TEXT,
started_at          TIMESTAMPTZ,
completed_at        TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_auto_sessions_user ON automation_sessions(user_id, created_at DESC);
CREATE INDEX idx_auto_sessions_state ON automation_sessions(state) WHERE state IN ('QUEUED', 'IN_PROGRESS', 'RETRYING');
CREATE INDEX idx_auto_sessions_application ON automation_sessions(application_id);
```

### 3.15 `interview_sessions` — Interview Hub

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
target_role         VARCHAR(200) NOT NULL,
target_company      VARCHAR(200),
mode                VARCHAR(10) NOT NULL DEFAULT 'TEXT',
                    -- TEXT, VOICE
status              VARCHAR(15) NOT NULL DEFAULT 'IN_PROGRESS',
                    -- IN_PROGRESS, COMPLETED, ABANDONED
questions           JSONB DEFAULT '[]'::jsonb,
                    -- [{"id": "...", "type": "BEHAVIORAL", "question": "Tell me about...",
                    --   "difficulty": 3, "order_index": 1, "score": null}, ...]
responses           JSONB DEFAULT '[]'::jsonb,
                    -- [{"question_id": "...", "text": "I handled...", "audio_url": "...",
                    --   "duration_sec": 45, "score": {"overall": 8, ...}, "feedback": "Good structure"}, ...]
overall_score       DECIMAL(4,1),             -- 0.0 - 10.0
feedback            JSONB,                    -- {"strengths": [...], "improvements": [...], "assessment": "...", "resources": [...]}
duration_seconds    INTEGER,
started_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
completed_at        TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_interview_sessions_user ON interview_sessions(user_id, created_at DESC);
```

### 3.16 `interview_question_bank` — Curated Questions

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
type                VARCHAR(20) NOT NULL,
                    -- BEHAVIORAL, TECHNICAL, SYSTEM_DESIGN, CODING, SITUATIONAL, DOMAIN
category            VARCHAR(100) NOT NULL,    -- e.g., "Leadership", "Java", "System Design"
question            TEXT NOT NULL,
difficulty          SMALLINT NOT NULL CHECK (difficulty BETWEEN 1 AND 5),
expected_answer     TEXT,                     -- ideal points to cover
tags                JSONB DEFAULT '[]'::jsonb,
source              VARCHAR(20) DEFAULT 'COMMUNITY',
                    -- COMMUNITY, AI_GENERATED, CURATED, COMPANY_SPECIFIC
company_id          UUID REFERENCES company_profiles(id) ON DELETE SET NULL,
times_used          INTEGER DEFAULT 0,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_question_bank_type ON interview_question_bank(type, difficulty);
CREATE INDEX idx_question_bank_company ON interview_question_bank(company_id) WHERE company_id IS NOT NULL;
CREATE INDEX idx_question_bank_tags ON interview_question_bank USING gin(tags);
```

### 3.17 `ats_analyses` — Resume ATS Scores

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
resume_id           UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
job_listing_id      UUID REFERENCES job_listings(id) ON DELETE CASCADE,
job_description_hash VARCHAR(64) NOT NULL,    -- SHA-256 of JD text for dedup
overall_score       INTEGER NOT NULL CHECK (overall_score BETWEEN 0 AND 100),
section_scores      JSONB,                   -- {"SUMMARY": 80, "EXPERIENCE": 75, ...}
keyword_matches     JSONB,                   -- {"Java": 5, "AWS": 3, ...}
missing_keywords    JSONB DEFAULT '[]'::jsonb,
weak_keywords       JSONB DEFAULT '[]'::jsonb,
keyword_density     DECIMAL(5,2),
format_score        INTEGER CHECK (format_score BETWEEN 0 AND 100),
formatting_issues   JSONB DEFAULT '[]'::jsonb,
suggestions         JSONB DEFAULT '[]'::jsonb,
                    -- [{"category": "KEYWORD", "severity": "MAJOR", "message": "Add 'Kubernetes'", "section": "SKILLS"}, ...]
analyzed_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_ats_analyses_resume ON ats_analyses(resume_id, analyzed_at DESC);
CREATE INDEX idx_ats_analyses_job_hash ON ats_analyses(resume_id, job_description_hash);
```

### 3.18 `prompt_templates` — Prompt Engine

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
use_case            VARCHAR(40) NOT NULL,
                    -- RESUME_TAILORING, RESUME_SCORING, COVER_LETTER, INTERVIEW_QUESTIONS,
                    -- ANSWER_SCORING, CAREER_PATH, SKILLS_GAP, NETWORKING_MESSAGE, JOB_MATCH
name                VARCHAR(200) NOT NULL,
version             INTEGER NOT NULL DEFAULT 1,
system_prompt       TEXT NOT NULL,
user_prompt_template TEXT NOT NULL,
variables           JSONB DEFAULT '[]'::jsonb,
                    -- [{"name": "user_profile", "type": "JSON", "required": true, "description": "..."}, ...]
model               VARCHAR(50) DEFAULT 'gpt-4',
temperature         DECIMAL(3,2) DEFAULT 0.7,
max_tokens          INTEGER DEFAULT 2048,
is_active           BOOLEAN DEFAULT TRUE,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Constraints:
UNIQUE (use_case, version)

-- Indexes:
CREATE INDEX idx_prompt_templates_active ON prompt_templates(use_case) WHERE is_active = TRUE;
```

### 3.19 `ai_usage_log` — AI Token Tracking

```sql
-- Design Notes:
-- - Append-only log for AI usage monitoring and billing
-- - Each row represents one LLM API call
-- - Cost tracked in micro-cents for precision

-- Columns:
id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
use_case                VARCHAR(40) NOT NULL,
provider                VARCHAR(20) NOT NULL,  -- OPENAI, ANTHROPIC, OLLAMA, GEMINI
model                   VARCHAR(50) NOT NULL,
prompt_tokens           INTEGER NOT NULL,
completion_tokens       INTEGER NOT NULL,
total_tokens            INTEGER NOT NULL,
cost_micro_usd          BIGINT NOT NULL,       -- cost in micro-cents (1/1,000,000 USD)
latency_ms              INTEGER,
cache_hit               BOOLEAN DEFAULT FALSE,
created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_ai_usage_user ON ai_usage_log(user_id, created_at);
CREATE INDEX idx_ai_usage_date ON ai_usage_log(created_at);
CREATE INDEX idx_ai_usage_provider ON ai_usage_log(provider, created_at);
```

### 3.20 `subscriptions` — Billing

```sql
-- Columns:
id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id                 UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
plan                    VARCHAR(20) NOT NULL DEFAULT 'FREE',
                        -- FREE, PRO, ENTERPRISE
stripe_customer_id      VARCHAR(100),
stripe_subscription_id  VARCHAR(100),
status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        -- ACTIVE, CANCELED, PAST_DUE, EXPIRED, TRIALING
current_period_start    TIMESTAMPTZ,
current_period_end      TIMESTAMPTZ,
trial_end               TIMESTAMPTZ,
canceled_at             TIMESTAMPTZ,
cancel_at_period_end    BOOLEAN DEFAULT FALSE,
created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_stripe ON subscriptions(stripe_customer_id);
```

### 3.21 `notifications` — Notification Service

```sql
-- Design Notes:
-- - Partitioned by month on created_at
-- - data column contains actionable payload for deep linking

-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
type                VARCHAR(40) NOT NULL,
                    -- APPLICATION_CONFIRMED, STATUS_CHANGE, AUTOMATION_COMPLETE,
                    -- NEW_JOB_MATCH, FOLLOW_UP, INTERVIEW_REMINDER, WELCOME, SYSTEM
title               VARCHAR(300) NOT NULL,
body                TEXT,
data                JSONB DEFAULT '{}'::jsonb,
                    -- {"application_id": "...", "deep_link": "/applications/...", "action_url": "..."}
channels            TEXT[] NOT NULL DEFAULT '{IN_APP}',
                    -- IN_APP, EMAIL, PUSH
status              VARCHAR(10) NOT NULL DEFAULT 'PENDING',
                    -- PENDING, SENT, READ, FAILED
read_at             TIMESTAMPTZ,
sent_at             TIMESTAMPTZ,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read_at, created_at DESC) WHERE read_at IS NULL;
CREATE INDEX idx_notifications_user_all ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_pending ON notifications(status, created_at) WHERE status = 'PENDING';
```

### 3.22 `audit_logs` — Security Audit Trail

```sql
-- Design Notes:
-- - Partitioned by month on created_at
-- - Immutable — never updated, only inserted
-- - Retention: 7 years (compliance requirement)

-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID REFERENCES users(id) ON DELETE SET NULL,
admin_user_id       UUID REFERENCES users(id) ON DELETE SET NULL,
action              VARCHAR(50) NOT NULL,
                    -- SUSPEND_USER, DELETE_USER, CHANGE_ROLE, CANCEL_SUB, etc.
resource_type       VARCHAR(50) NOT NULL,     -- USER, APPLICATION, SUBSCRIPTION, etc.
resource_id         VARCHAR(100) NOT NULL,
old_value           JSONB,
new_value           JSONB,
ip_address          INET,
user_agent          TEXT,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()

-- Indexes:
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id, created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, created_at);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
```

### 3.23 `outbox` — Transactional Outbox

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
aggregate_type      VARCHAR(50) NOT NULL,     -- Application, User, Resume, etc.
aggregate_id        UUID NOT NULL,
event_type          VARCHAR(100) NOT NULL,    -- fully qualified class name
event_payload       JSONB NOT NULL,
trace_id            VARCHAR(64),              -- OpenTelemetry trace ID
status              VARCHAR(10) NOT NULL DEFAULT 'PENDING',
                    -- PENDING, PUBLISHED, FAILED
retry_count         INTEGER DEFAULT 0,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
published_at        TIMESTAMPTZ

-- Indexes:
CREATE INDEX idx_outbox_pending ON outbox(status, created_at) WHERE status = 'PENDING';
```

### 3.24 `user_settings` — User Preferences

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
user_id             UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
theme               VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
                    -- LIGHT, DARK, SYSTEM
language            VARCHAR(10) NOT NULL DEFAULT 'en',
timezone            VARCHAR(50) NOT NULL DEFAULT 'UTC',
ai_preferences      JSONB DEFAULT '{}'::jsonb,
                    -- {"preferred_provider": "OPENAI", "fallback_provider": "ANTHROPIC",
                    --  "max_tokens_month": 1000000, "enable_ai_suggestions": true, "auto_tailor": false}
notification_prefs  JSONB DEFAULT '{}'::jsonb,
                    -- {"channels": {"IN_APP": true, "EMAIL": true, "PUSH": false},
                    --  "digest": "DAILY", "quiet_hours_start": "22:00", "quiet_hours_end": "08:00"}
privacy_settings    JSONB DEFAULT '{}'::jsonb,
                    -- {"share_profile": false, "anonymize": true, "data_training": false, "visibility": "PRIVATE"}
display_settings    JSONB DEFAULT '{}'::jsonb,
                    -- {"compact_mode": false, "show_salary": true, "pipeline_view": "KANBAN"}
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

### 3.25 `feature_flags` — Admin Feature Toggle

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
key                 VARCHAR(100) NOT NULL UNIQUE,
enabled             BOOLEAN NOT NULL DEFAULT FALSE,
description         TEXT,
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

### 3.26 `job_source_configs` — Admin Job Source Management

```sql
-- Columns:
id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
name                VARCHAR(100) NOT NULL UNIQUE,
adapter_class       VARCHAR(255) NOT NULL,    -- fully qualified adapter class name
enabled             BOOLEAN DEFAULT TRUE,
config              JSONB DEFAULT '{}'::jsonb,
                    -- {"api_key_ref": "vault://indeed/api_key", "base_url": "...",
                    --  "rate_limit_per_min": 10, "proxies": ["..."]}
last_run_at         TIMESTAMPTZ,
last_run_status     VARCHAR(20),              -- SUCCESS, FAILED, RUNNING
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

---

## 4. Index Strategy

### 4.1 Index Categories

| Type | Indexes | Purpose |
|------|---------|---------|
| **B-Tree Primary** | PKs, UKs, FK | Fast point lookups, unique enforcement |
| **B-Tree Filter** | `WHERE deleted_at IS NULL` | Soft-delete filtering |
| **B-Tree Composite** | `(user_id, status)`, `(user_id, created_at DESC)` | Common query patterns |
| **B-Tree Range** | `(created_at)`, `(posted_at DESC)` | Date-range queries, sorting |
| **GIN** | JSONB columns (`skills`, `technology_stack`) | JSON containment queries |
| **GIN (tsvector)** | `to_tsvector(column)` on `job_listings` | Full-text search |
| **IVFFlat** | `embeddings vector_cosine_ops` on `job_listings` | Semantic search (pgvector) |
| **Partial** | `WHERE status = 'PENDING'` on `outbox`, `WHERE is_active = TRUE` on `job_listings` | Focused index size |

### 4.2 Performance-Critical Queries & Their Indexes

| Query | Index | Type |
|-------|-------|------|
| `SELECT * FROM users WHERE email = ? AND deleted_at IS NULL` | `idx_users_email` (partial) | B-Tree |
| `SELECT * FROM job_listings WHERE to_tsvector('english', title \|\| ' ' \|\| company) @@ plainto_tsquery(?) ORDER BY posted_at DESC LIMIT 20` | `idx_job_listings_fts` (GIN) + `idx_job_listings_posted` | GIN + B-Tree |
| `SELECT * FROM applications WHERE user_id = ? AND status = 'APPLIED' AND deleted_at IS NULL` | `idx_applications_user_status` (composite, partial) | B-Tree |
| `SELECT * FROM notifications WHERE user_id = ? AND read_at IS NULL ORDER BY created_at DESC LIMIT 20` | `idx_notifications_user_unread` (composite, partial) | B-Tree |
| `SELECT * FROM job_listings WHERE embeddings <=> ? < 0.5 ORDER BY embeddings <=> ? LIMIT 20` | `idx_job_listings_embeddings` (IVFFlat) | Vector |
| `SELECT * FROM outbox WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT 100 FOR UPDATE SKIP LOCKED` | `idx_outbox_pending` (partial) | B-Tree |
| `SELECT * FROM automation_sessions WHERE state IN ('QUEUED', 'IN_PROGRESS', 'RETRYING')` | `idx_auto_sessions_state` (partial) | B-Tree |

### 4.3 Index Maintenance

```sql
-- Reindex during low-traffic window (weekly):
REINDEX INDEX CONCURRENTLY idx_job_listings_fts;
REINDEX INDEX CONCURRENTLY idx_job_listings_embeddings;

-- Monitor unused indexes (monthly):
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND indexdef NOT LIKE '%PRIMARY%';

-- Monitor missing indexes:
SELECT * FROM pg_stat_user_tables WHERE seq_scan > 1000;
```

---

## 5. Partitioning Strategy

### 5.1 Partitioned Tables

| Table | Partition Key | Type | Interval | Retention |
|-------|--------------|------|----------|-----------|
| `notifications` | `created_at` | Range | Monthly | 12 months |
| `audit_logs` | `created_at` | Range | Monthly | 84 months (7 years) |
| `job_listings` | `scraped_at` | Range | Monthly | 24 months |
| `ai_usage_log` | `created_at` | Range | Monthly | 12 months |
| `outbox` | `created_at` | Range | Monthly | 3 months |

### 5.2 Partitioning Example (Notifications)

```sql
-- Parent table
CREATE TABLE notifications (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(300) NOT NULL,
    body TEXT,
    data JSONB DEFAULT '{}'::jsonb,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- Monthly partitions
CREATE TABLE notifications_2026_01 PARTITION OF notifications
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE notifications_2026_02 PARTITION OF notifications
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
CREATE TABLE notifications_2026_03 PARTITION OF notifications
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
-- ... automated creation via pg_partman or maintenance job

-- Indexes on each partition (or on parent with auto-indexing)
CREATE INDEX idx_notifications_2026_01_user_unread
    ON notifications_2026_01(user_id, read_at, created_at DESC) WHERE read_at IS NULL;
```

### 5.3 Partition Management (Automated)

```sql
-- Create future partitions (monthly job):
SELECT partman.create_parent(
    p_parent_table := 'public.notifications',
    p_control := 'created_at',
    p_type := 'native',
    p_interval := '1 month',
    p_premake := 3
);

-- Detach and archive old partitions (quarterly job):
-- For data > 12 months for notifications:
--   1. Detach partition
--   2. Dump to S3 as Parquet
--   3. Drop partition
```

---

## 6. Full-Text Search Schema

### 6.1 Search Configuration

```sql
-- Job listings search vector (updated via trigger)
CREATE OR REPLACE FUNCTION update_job_listings_tsvector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := to_tsvector('english',
        coalesce(NEW.title, '') || ' ' ||
        coalesce(NEW.company_name, '') || ' ' ||
        coalesce(NEW.description, '')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_job_listings_tsvector
    BEFORE INSERT OR UPDATE ON job_listings
    FOR EACH ROW EXECUTE FUNCTION update_job_listings_tsvector();

-- Weighted search query pattern:
SELECT id, title, company_name, posted_at,
    ts_rank(search_vector, plainto_tsquery('english', 'senior java engineer remote'), 1) AS rank,
    ts_headline('english', description, plainto_tsquery('english', 'senior java engineer remote'),
                'StartSel=<mark>, StopSel=</mark>, MaxWords=50, MinWords=20') AS snippet
FROM job_listings
WHERE search_vector @@ plainto_tsquery('english', 'senior java engineer remote')
    AND is_active = TRUE
    AND salary_min >= 100000
    AND location->>'remote' = 'REMOTE'
ORDER BY rank DESC
LIMIT 20;

-- Faceted counts (for filter sidebar):
SELECT experience_level, COUNT(*) as count
FROM job_listings
WHERE search_vector @@ plainto_tsquery('english', 'java')
    AND is_active = TRUE
GROUP BY experience_level;
```

### 6.2 Search Weight Tiers

| Field | Weight | Factor |
|-------|--------|--------|
| title | A | 1.0 |
| company_name | B | 0.8 |
| skills (extracted) | B | 0.7 |
| description | C | 0.3 |
| requirements | C | 0.4 |

---

## 7. Vector Search Schema (pgvector)

### 7.1 Extension Setup

```sql
CREATE EXTENSION IF NOT EXISTS vector;

-- Vector dimension: 1536 (OpenAI text-embedding-3-large compatible)
-- Also supports: 512 (text-embedding-3-small), 768 (Cohere), 1024 (BGE)
```

### 7.2 Index Configuration

```sql
-- IVFFlat index (Inverted File with Flat compression)
-- lists = sqrt(number_of_rows) — for 1M rows: lists = 1000
-- probes = lists / 10 — for 1000 lists: probes = 100

CREATE INDEX idx_job_listings_embeddings
    ON job_listings
    USING ivfflat (embeddings vector_cosine_ops)
    WITH (lists = 100);

-- Query configuration:
SET ivfflat.probes = 10;  -- balance between speed and recall

-- Alternative: HNSW index (for higher recall, slower build)
-- CREATE INDEX idx_job_listings_embeddings_hnsw
--     ON job_listings
--     USING hnsw (embeddings vector_cosine_ops)
--     WITH (m = 16, ef_construction = 200);
```

### 7.3 Hybrid Search Query

```sql
-- Full-text + Vector hybrid search (weighted combination)
WITH fts_results AS (
    SELECT id, title, company_name,
        ts_rank(search_vector, plainto_tsquery('english', 'senior java')) AS text_score
    FROM job_listings
    WHERE search_vector @@ plainto_tsquery('english', 'senior java')
        AND is_active = TRUE
),
vector_results AS (
    SELECT id,
        1 - (embeddings <=> '[0.01, 0.02, ...1536 dims...]'::vector) AS vector_score
    FROM job_listings
    WHERE is_active = TRUE
    ORDER BY embeddings <=> '[0.01, 0.02, ...]'::vector
    LIMIT 200
)
SELECT f.id, f.title, f.company_name,
    (0.6 * f.text_score + 0.4 * v.vector_score) AS combined_score
FROM fts_results f
JOIN vector_results v ON f.id = v.id
ORDER BY combined_score DESC
LIMIT 20;
```

---

## 8. Migration Strategy

### 8.1 Tooling

| Tool | Purpose | Decision |
|------|---------|----------|
| **Flyway** | Schema migrations | Version-controlled, repeatable, widely adopted |
| **pg_partman** | Automated partition management | Handles partition creation/detach automatically |

### 8.2 Migration Naming

```
V1__init_schema.sql              — Initial schema creation
V2__add_embeddings_column.sql     — Add pgvector column
V3__create_partitions.sql         — Set up partitioning
V4__add_search_indexes.sql       — Add GIN/IVFFlat indexes
V5__encrypt_existing_phones.sql  — Data migration for PII
V6__seed_prompt_templates.sql    — Seed initial prompt data
```

### 8.3 Migration Principles

| # | Principle |
|---|-----------|
| 1 | Every migration must be reversible (provide undo script in comments) |
| 2 | Migrations must be additive (never delete columns — use `deprecated_at`) |
| 3 | Data migrations must be in separate scripts from schema changes |
| 4 | Indexes created CONCURRENTLY (no table locks) |
| 5 | Large table migrations use batching (batched UPDATE/DELETE) |
| 6 | Zero-downtime: add columns as nullable first, then backfill |

### 8.4 Zero-Downtime Migration Pattern

```sql
-- 1. Add column as nullable
ALTER TABLE job_listings ADD COLUMN industry VARCHAR(100);

-- 2. Backfill data (batched)
DO $$
DECLARE
    batch_size CONSTANT INTEGER := 1000;
    affected INTEGER;
BEGIN
    LOOP
        UPDATE job_listings
        SET industry = extract_industry(description)
        WHERE id IN (
            SELECT id FROM job_listings
            WHERE industry IS NULL
            LIMIT batch_size
            FOR UPDATE SKIP LOCKED
        );
        GET DIAGNOSTICS affected = ROW_COUNT;
        EXIT WHEN affected = 0;
        COMMIT;
    END LOOP;
END $$;

-- 3. Add NOT NULL constraint
ALTER TABLE job_listings ALTER COLUMN industry SET NOT NULL;

-- 4. Add index (CONCURRENTLY — no table lock)
CREATE INDEX CONCURRENTLY idx_job_listings_industry ON job_listings(industry);
```

---

## 9. Read Replica Schema

### 9.1 Read Replica Query Routing

| Query Pattern | Target | Reason |
|---------------|--------|--------|
| `SELECT` — dashboard, analytics | Read replica | Heavy aggregation, no transaction needed |
| `SELECT` — job search | Read replica | High frequency, stale data acceptable (seconds) |
| `SELECT` — user profile, application detail | Read replica | Can tolerate <100ms lag |
| `INSERT/UPDATE/DELETE` — all writes | Primary | Must be on primary for consistency |
| `SELECT` — within `@Transactional` | Primary | Read-your-writes consistency |
| Authentication queries | Primary | Must see latest user state (lock, role changes) |

### 9.2 Configuration Approach

```sql
-- Spring datasource configuration (conceptual):
-- Primary: write datasource (HikariCP, max 200)
-- Replica: read datasource (HikariCP, max 300)
-- @Transactional(readOnly = true) → replica
-- @Transactional (no readonly) → primary
-- LOB handling: prefer replica for large JSONB/tsvector queries
```

### 9.3 Read Replica Indexes

Replicas should have all indexes from primary. Additionally:

```sql
-- Materialized view for analytics (refreshed daily on replica)
CREATE MATERIALIZED VIEW mv_daily_analytics AS
SELECT
    u.id AS user_id,
    DATE(a.created_at) AS date,
    COUNT(DISTINCT a.id) AS applications_sent,
    COUNT(DISTINCT CASE WHEN a.status IN ('OFFER', 'ACCEPTED') THEN a.id END) AS offers,
    COUNT(DISTINCT i.id) AS interview_sessions
FROM users u
LEFT JOIN applications a ON a.user_id = u.id AND a.deleted_at IS NULL
LEFT JOIN interview_sessions i ON i.user_id = u.id
WHERE u.deleted_at IS NULL
GROUP BY u.id, DATE(a.created_at);

REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_analytics;
```

---

## 10. Optimization Strategy

### 10.1 Connection Pool Management

| Pool | Min | Max | Timeout | Max Lifetime |
|------|-----|-----|---------|--------------|
| Primary | 10 | 200 | 30s | 30 min |
| Read Replica | 10 | 300 | 10s | 30 min |
| Flyway (migration) | 1 | 1 | 60s | 5 min |

### 10.2 Query Optimization Rules

| Rule | Technique |
|------|-----------|
| **N+1 Prevention** | Always use JOIN FETCH or EntityGraph for relationships |
| **Batch Loading** | Use `@BatchSize(size = 50)` on collections |
| **Pagination** | Cursor-based (keyset) pagination, not OFFSET |
| **JSONB Access** | Use `->>` not `->` for text values (avoids type coercion) |
| **GIN vs B-Tree** | JSONB containment queries → GIN; equality/range → B-Tree |
| **Partial Indexes** | Every index should have a WHERE clause when possible |
| **Covering Indexes** | Include columns to avoid heap lookups |

### 10.3 Cursor-Based Pagination

```sql
-- Instead of: SELECT * FROM applications WHERE user_id = ? ORDER BY created_at DESC LIMIT 20 OFFSET 100
-- (OFFSET forces scan of skipped rows)

-- Use keyset pagination:
SELECT id, user_id, status, created_at
FROM applications
WHERE user_id = ?
  AND created_at < :cursor   -- last item's created_at from previous page
ORDER BY created_at DESC
LIMIT 20;

-- Cursor is the last item's sort value (opaque, base64-encoded)
```

### 10.4 Vacuum & Analyze Strategy

| Table | Vacuum Schedule | Analyze Schedule |
|-------|----------------|-----------------|
| `job_listings` | Daily (low traffic window) | After every bulk insert |
| `applications` | Weekly | Weekly |
| `notifications` | Weekly | Monthly |
| `audit_logs` | Monthly | Monthly |
| `ai_usage_log` | Weekly | Weekly |
| All others | Auto-vacuum (default) | Auto-analyze (default) |

### 10.5 Memory Configuration

```sql
-- PostgreSQL configuration targets (for 16GB RAM, 4 vCPU instance):
shared_buffers = 4GB              -- 25% of RAM
effective_cache_size = 12GB       -- 75% of RAM
work_mem = 64MB                    -- per-operation sort memory
maintenance_work_mem = 1GB         -- for VACUUM, CREATE INDEX
wal_buffers = 64MB
random_page_cost = 1.1             -- SSD optimization
effective_io_concurrency = 200     -- SSD optimization
max_worker_processes = 8
max_parallel_workers_per_gather = 4
max_parallel_workers = 8
```

---

## 11. Backup & Recovery

### 11.1 Backup Schedule

| Type | Frequency | Retention | Method |
|------|-----------|-----------|--------|
| Full backup | Daily | 30 days | `pg_dump` (custom format) |
| WAL archiving | Continuous | 7 days | Archive to S3 (WAL-G) |
| Point-in-time recovery | N/A | 7 days | WAL replay |
| Logical backup (schema) | Weekly | 90 days | `pg_dump --schema-only` |

### 11.2 Recovery Time Objectives

| Scenario | RTO | RPO |
|----------|-----|-----|
| Single table corruption | < 1 hour | < 5 min |
| Entire instance failure | < 30 min | < 1 min (WAL streaming) |
| Region failure | < 4 hours | < 15 min (cross-region replica) |

### 11.3 Backup Commands

```bash
# Full backup (custom format — compressed, parallel)
pg_dump -h localhost -U jobpilot -Fc -j 4 -f backup_$(date +%Y%m%d).dump jobpilot_db

# Restore
pg_restore -h localhost -U jobpilot -d jobpilot_db -j 4 backup_20260701.dump

# WAL archiving via WAL-G (to S3)
wal-g backup-push /var/lib/postgresql/16/main
wal-g backup-fetch /var/lib/postgresql/16/main LATEST
```

---

## 12. Appendix: Data Dictionary

### Entity Count Summary

| Module | Tables | Est. Row Growth | Description |
|--------|--------|-----------------|-------------|
| Identity & Access | 2 | User: +1000/day | Users, profiles |
| Resume Studio | 3 | Resume: +500/day | Resumes, cover letters, ATS analyses |
| Job Discovery | 5 | Listings: +10k/day | Job listings, skills, saved jobs/searches |
| Application Tracker | 5 | Apps: +2000/day | Applications, notes, attachments, timeline |
| Company Intelligence | 1 | Companies: +100/day | Company profiles |
| Interview Hub | 2 | Sessions: +300/day | Sessions, question bank |
| Browser Automation | 1 | Sessions: +1000/day | Automation sessions |
| AI & Prompts | 3 | AI logs: +5000/day | Prompt templates, AI usage, outbox |
| Notifications | 1 | Notifications: +20k/day | Notifications |
| Billing | 1 | Minimal | Subscriptions |
| Admin | 3 | Minimal | Audit logs, feature flags, source configs |
| Settings | 1 | 1 per user | User preferences |

### Column Type Conventions

| PostgreSQL Type | Usage |
|----------------|--------|
| `UUID` | All primary keys, foreign keys |
| `TIMESTAMPTZ` | All timestamps (always with timezone) |
| `VARCHAR(N)` | Fixed-length strings (emails, URLs) |
| `TEXT` | Variable-length content (descriptions, prompts) |
| `NUMERIC(12,2)` | Monetary values |
| `JSONB` | Semi-structured data, flexible schemas |
| `vector(1536)` | Embeddings for semantic search |
| `INET` | IP addresses (audit logs, access logs) |
| `BYTEA` | Encrypted PII (phone numbers) |
| `BOOLEAN` | Flags, toggles |
| `SMALLINT` | Small numeric ranges (0-100 scores) |

---

*This Database Design covers all 26+ tables across 12 modules, with partitioning, indexing, search, vector, migration, optimization, and backup strategies. It follows PostgreSQL best practices for a production SaaS platform handling 10k+ new rows per day.*

---

**End of Database Design v1.0**
