-- Invoices
CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES subscriptions(id),
    amount          DECIMAL(10,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description     VARCHAR(255),
    pdf_url         VARCHAR(512),
    paid_at         TIMESTAMPTZ,
    due_at          TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_user ON invoices(user_id);
CREATE INDEX idx_invoices_status ON invoices(status);

-- Application Follow-ups
CREATE TABLE application_followups (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v7(),
    application_id  UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL DEFAULT 'FOLLOW_UP',
    subject         VARCHAR(255),
    content         TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_at    TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_followups_application ON application_followups(application_id);
CREATE INDEX idx_followups_user ON application_followups(user_id);
