CREATE TABLE automation_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    step VARCHAR(100),
    progress INTEGER NOT NULL DEFAULT 0,
    screenshots JSONB DEFAULT '[]'::jsonb,
    error_message TEXT,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_automation_sessions_user_id ON automation_sessions(user_id);
CREATE INDEX idx_automation_sessions_status ON automation_sessions(status);

CREATE TRIGGER trg_automation_sessions_updated_at
    BEFORE UPDATE ON automation_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
