DROP TABLE IF EXISTS interview_sessions CASCADE;

CREATE TABLE interview_sessions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id          UUID,
    job_id              UUID,
    type                VARCHAR(50) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    duration_minutes    INTEGER,
    interviewer_name    VARCHAR(255),
    interview_round     INTEGER,
    location            VARCHAR(255),
    meeting_link        VARCHAR(512),
    notes               TEXT,
    feedback            TEXT,
    rating              INTEGER,
    questions           JSONB NOT NULL DEFAULT '[]',
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_interviews_user ON interview_sessions(user_id);
CREATE INDEX idx_interviews_company ON interview_sessions(company_id);
