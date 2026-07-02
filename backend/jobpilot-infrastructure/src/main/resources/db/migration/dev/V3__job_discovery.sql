-- ============================================================================
-- Job Discovery Tables
-- ============================================================================

-- Job Listings (aggregated from multiple sources)
CREATE TABLE job_listings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    source          VARCHAR(50) NOT NULL,
    source_id       VARCHAR(255),
    title           VARCHAR(255) NOT NULL,
    company_name    VARCHAR(255) NOT NULL,
    company_logo_url VARCHAR(512),
    company_id      UUID,
    location        JSONB NOT NULL DEFAULT '{}',
    salary          JSONB,
    description     TEXT NOT NULL,
    requirements    JSONB DEFAULT '[]',
    responsibilities JSONB DEFAULT '[]',
    benefits        JSONB DEFAULT '[]',
    employment_type employment_type,
    experience_level experience_level,
    industry        VARCHAR(100),
    skills          JSONB DEFAULT '[]',
    application_url VARCHAR(512),
    posted_at       TIMESTAMPTZ,
    scraped_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    embeddings      vector(1536),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_jobs_source ON job_listings(source, source_id);
CREATE INDEX idx_jobs_company ON job_listings(company_name);
CREATE INDEX idx_jobs_posted ON job_listings(posted_at DESC);
CREATE INDEX idx_jobs_active ON job_listings(is_active);
CREATE INDEX idx_jobs_skills ON job_listings USING GIN(skills);
CREATE INDEX idx_jobs_fts ON job_listings USING GIN(
    to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, ''))
);
CREATE INDEX idx_jobs_embeddings ON job_listings USING ivfflat (embeddings vector_cosine_ops) WITH (lists = 100);

-- Full text search materialized column
ALTER TABLE job_listings ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, '') || ' ' || coalesce(company_name, ''))) STORED;

-- Saved Jobs
CREATE TABLE saved_jobs (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_listing_id  UUID NOT NULL REFERENCES job_listings(id) ON DELETE CASCADE,
    notes           TEXT,
    saved_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, job_listing_id)
);

-- Saved Search Queries
CREATE TABLE saved_searches (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    query_params    JSONB NOT NULL,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notify_frequency VARCHAR(20) NOT NULL DEFAULT 'DAILY',
    last_notified_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_saved_searches_user ON saved_searches(user_id);

-- Job Source Configurations
CREATE TABLE job_source_configs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    source_name     VARCHAR(50) NOT NULL UNIQUE,
    adapter_class   VARCHAR(255) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    config          JSONB NOT NULL DEFAULT '{}',
    rate_limit_per_sec INTEGER DEFAULT 1,
    last_run_at     TIMESTAMPTZ,
    last_run_status VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
