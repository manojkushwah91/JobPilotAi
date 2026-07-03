CREATE TABLE scheduled_tasks (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_type       VARCHAR(100) NOT NULL,
    payload         JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_at    TIMESTAMPTZ NOT NULL,
    executed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_scheduled_tasks_status ON scheduled_tasks(status, scheduled_at);
CREATE INDEX idx_scheduled_tasks_user ON scheduled_tasks(user_id);
