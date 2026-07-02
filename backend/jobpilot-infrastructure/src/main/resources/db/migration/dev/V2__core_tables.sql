-- ============================================================================
-- Core Business Tables
-- ============================================================================

-- Users & Authentication
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    name            VARCHAR(255) NOT NULL,
    role            user_role NOT NULL DEFAULT 'FREE',
    tier            VARCHAR(20) NOT NULL DEFAULT 'FREE',
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url      VARCHAR(512),
    locale          VARCHAR(10) DEFAULT 'en',
    last_login_at   TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- OAuth accounts
CREATE TABLE oauth_accounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider        VARCHAR(50) NOT NULL,
    provider_id     VARCHAR(255) NOT NULL,
    access_token    TEXT,
    refresh_token   TEXT,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_oauth_provider ON oauth_accounts(provider, provider_id);
CREATE INDEX idx_oauth_user_id ON oauth_accounts(user_id);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL,
    family          VARCHAR(100) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- User Profiles
CREATE TABLE user_profiles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    headline        VARCHAR(255),
    bio             TEXT,
    phone_number    VARCHAR(50),
    location_city   VARCHAR(100),
    location_state  VARCHAR(100),
    location_country VARCHAR(100),
    linkedin_url    VARCHAR(512),
    github_url      VARCHAR(512),
    portfolio_url   VARCHAR(512),
    current_company VARCHAR(255),
    years_experience INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON user_profiles FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- User Settings
CREATE TABLE user_settings (
    user_id             UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    job_preferences     JSONB NOT NULL DEFAULT '{}',
    notification_prefs  JSONB NOT NULL DEFAULT '{}',
    privacy_settings    JSONB NOT NULL DEFAULT '{}',
    ai_preferences      JSONB NOT NULL DEFAULT '{}',
    appearance          JSONB NOT NULL DEFAULT '{}',
    two_factor_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_method   VARCHAR(20),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Resumes
CREATE TABLE resumes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    ats_score       INTEGER,
    ats_score_data  JSONB,
    version         INTEGER NOT NULL DEFAULT 1,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_resumes_user ON resumes(user_id);
CREATE INDEX idx_resumes_deleted ON resumes(deleted_at);

CREATE TRIGGER trg_resumes_updated_at
    BEFORE UPDATE ON resumes FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Resume Sections
CREATE TABLE resume_sections (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    resume_id       UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL,
    title           VARCHAR(255),
    content         JSONB NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_resume_sections_resume ON resume_sections(resume_id);

-- Resume Versions (full version history)
CREATE TABLE resume_versions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    resume_id       UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    version         INTEGER NOT NULL,
    sections_data   JSONB NOT NULL,
    change_notes    TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_resume_versions_resume ON resume_versions(resume_id);

-- Cover Letters
CREATE TABLE cover_letters (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resume_id       UUID REFERENCES resumes(id),
    title           VARCHAR(255) NOT NULL,
    company_name    VARCHAR(255) NOT NULL,
    recipient_name  VARCHAR(255),
    body            JSONB NOT NULL,
    tone            VARCHAR(50) NOT NULL DEFAULT 'PROFESSIONAL',
    word_count      INTEGER NOT NULL DEFAULT 0,
    version         INTEGER NOT NULL DEFAULT 1,
    file_url        VARCHAR(512),
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_cover_letters_user ON cover_letters(user_id);
CREATE INDEX idx_cover_letters_deleted ON cover_letters(deleted_at);

CREATE TRIGGER trg_cover_letters_updated_at
    BEFORE UPDATE ON cover_letters FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
