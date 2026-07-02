-- ============================================================================
-- Interview Hub & Company Intelligence Tables
-- ============================================================================

-- Interview Sessions
CREATE TABLE interview_sessions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_role         VARCHAR(255) NOT NULL,
    target_company      VARCHAR(255),
    mode                VARCHAR(10) NOT NULL DEFAULT 'TEXT',
    status              VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    questions           JSONB NOT NULL DEFAULT '[]',
    current_question_index INTEGER NOT NULL DEFAULT 0,
    overall_score       DECIMAL(4,2),
    feedback            JSONB,
    duration_seconds    INTEGER,
    started_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_interviews_user ON interview_sessions(user_id);

-- Interview Question Bank
CREATE TABLE interview_question_bank (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    type            VARCHAR(50) NOT NULL,
    category        VARCHAR(100) NOT NULL,
    question        TEXT NOT NULL,
    difficulty      INTEGER NOT NULL DEFAULT 1,
    expected_answer TEXT,
    tags            JSONB DEFAULT '[]',
    source          VARCHAR(100),
    company_id      UUID,
    times_used      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_question_type ON interview_question_bank(type);
CREATE INDEX idx_question_category ON interview_question_bank(category);
CREATE INDEX idx_question_difficulty ON interview_question_bank(difficulty);
CREATE INDEX idx_question_company ON interview_question_bank(company_id);

-- Company Profiles
CREATE TABLE company_profiles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    name            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    website         VARCHAR(512),
    logo_url        VARCHAR(512),
    industry        VARCHAR(100),
    headquarters    JSONB,
    founded_year    SMALLINT,
    company_size_min INTEGER,
    company_size_max INTEGER,
    stock_symbol    VARCHAR(20),
    funding_rounds  JSONB DEFAULT '[]',
    technology_stack JSONB DEFAULT '[]',
    culture_keywords JSONB DEFAULT '[]',
    benefits        JSONB DEFAULT '[]',
    interview_notes JSONB DEFAULT '[]',
    salary_data     JSONB DEFAULT '[]',
    hiring_trends   JSONB,
    glassdoor_rating DECIMAL(3,2),
    linkedin_url    VARCHAR(512),
    crunchbase_url  VARCHAR(512),
    last_enriched_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_companies_name ON company_profiles(name);
CREATE INDEX idx_companies_industry ON company_profiles(industry);
CREATE INDEX idx_companies_tech ON company_profiles USING GIN(technology_stack);

CREATE TRIGGER trg_companies_updated_at
    BEFORE UPDATE ON company_profiles FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
