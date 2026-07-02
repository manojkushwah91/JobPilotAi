-- ============================================================================
-- Application Tracker Tables
-- ============================================================================

CREATE TABLE applications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_listing_id  UUID REFERENCES job_listings(id),
    resume_id       UUID REFERENCES resumes(id),
    cover_letter_id UUID REFERENCES cover_letters(id),
    status          application_status NOT NULL DEFAULT 'SAVED',
    status_history  JSONB NOT NULL DEFAULT '[]',
    automation_info JSONB,
    salary_offered  JSONB,
    applied_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_applications_user ON applications(user_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_job ON applications(job_listing_id);
CREATE INDEX idx_applications_deleted ON applications(deleted_at);

CREATE TRIGGER trg_applications_updated_at
    BEFORE UPDATE ON applications FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Application Notes
CREATE TABLE application_notes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    application_id  UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    category        VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_app_notes_application ON application_notes(application_id);

-- Application Attachments
CREATE TABLE application_attachments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    application_id  UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    file_type       VARCHAR(100) NOT NULL,
    file_url        VARCHAR(512) NOT NULL,
    file_size       INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_app_attachments_application ON application_attachments(application_id);
