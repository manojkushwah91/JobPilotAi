ALTER TABLE email_verification_tokens ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE password_reset_tokens ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
