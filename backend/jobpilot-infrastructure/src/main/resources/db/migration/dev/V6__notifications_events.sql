-- ============================================================================
-- Notifications, Events, and Outbox Tables
-- ============================================================================

-- Notifications
CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL,
    channel         notification_channel NOT NULL DEFAULT 'IN_APP',
    title           VARCHAR(255) NOT NULL,
    body            TEXT NOT NULL,
    metadata        JSONB DEFAULT '{}',
    status          notification_status NOT NULL DEFAULT 'PENDING',
    read_at         TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, status);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

-- Notification Templates (system data)
CREATE TABLE notification_templates (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    type            VARCHAR(50) NOT NULL,
    channel         notification_channel NOT NULL,
    subject         VARCHAR(255),
    body_template   TEXT NOT NULL,
    variables       JSONB DEFAULT '[]',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_notif_template ON notification_templates(type, channel);

-- Outbox (Transactional Outbox pattern)
CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(50) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    trace_id        VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status ON outbox_events(status, created_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);

-- Prompt Templates (AI Engine)
CREATE TABLE prompt_templates (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    use_case        VARCHAR(50) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    version         INTEGER NOT NULL,
    system_prompt   TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL,
    variables       JSONB DEFAULT '[]',
    model           VARCHAR(100),
    temperature     DECIMAL(3,2) DEFAULT 0.7,
    max_tokens      INTEGER DEFAULT 2048,
    is_active       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prompt_use_case ON prompt_templates(use_case, version);

-- AI Usage Tracking
CREATE TABLE ai_usage_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID REFERENCES users(id),
    use_case        VARCHAR(50) NOT NULL,
    provider        VARCHAR(50) NOT NULL,
    model           VARCHAR(100) NOT NULL,
    prompt_tokens   INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens    INTEGER NOT NULL DEFAULT 0,
    cost_micro_usd  BIGINT NOT NULL DEFAULT 0,
    latency_ms      INTEGER NOT NULL DEFAULT 0,
    cache_hit       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_usage_created ON ai_usage_logs(created_at DESC);
CREATE INDEX idx_ai_usage_user ON ai_usage_logs(user_id);
CREATE INDEX idx_ai_usage_provider ON ai_usage_logs(provider);

-- Feature Flags
CREATE TABLE feature_flags (
    key             VARCHAR(100) PRIMARY KEY,
    enabled         BOOLEAN NOT NULL DEFAULT FALSE,
    description     TEXT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Admin Audit Log
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    actor_id        UUID REFERENCES users(id),
    actor_email     VARCHAR(255),
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(100) NOT NULL,
    resource_id     VARCHAR(50),
    details         JSONB,
    ip_address      VARCHAR(50),
    user_agent      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_actor ON audit_logs(actor_id, created_at DESC);
CREATE INDEX idx_audit_action ON audit_logs(action, created_at DESC);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);

-- Schema version tracking
CREATE TABLE schema_version (
    version         VARCHAR(50) PRIMARY KEY,
    applied_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    description     TEXT
);
