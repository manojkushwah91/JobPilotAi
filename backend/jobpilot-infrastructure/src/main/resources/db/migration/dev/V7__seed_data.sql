-- ============================================================================
-- Development Seed Data
-- ============================================================================

-- Feature Flags
INSERT INTO feature_flags (key, enabled, description) VALUES
    ('resume-ai-scoring', true, 'Enable AI-powered resume scoring'),
    ('cover-letter-generation', true, 'Enable AI cover letter generation'),
    ('browser-automation', true, 'Enable browser automation for job applications'),
    ('interview-hub', true, 'Enable mock interview functionality'),
    ('company-intelligence', true, 'Enable company research enrichment'),
    ('career-analytics', true, 'Enable analytics dashboard'),
    ('semantic-search', true, 'Enable semantic job search'),
    ('dark-mode', true, 'Enable dark mode UI toggle');

-- Notification Templates
INSERT INTO notification_templates (type, channel, subject, body_template, variables) VALUES
    ('APPLICATION_STATUS_CHANGE', 'EMAIL', 'Application Status Update',
     'Your application for {{job_title}} at {{company}} has moved to {{status}}.',
     '["job_title", "company", "status"]'),
    ('APPLICATION_STATUS_CHANGE', 'IN_APP', NULL,
     'Application moved to {{status}}: {{job_title}} at {{company}}',
     '["job_title", "company", "status"]'),
    ('NEW_JOB_MATCH', 'EMAIL', 'New Job Match Found!',
     'We found a new job that matches your profile: {{job_title}} at {{company}} ({{match_score}}% match).',
     '["job_title", "company", "match_score", "job_url"]'),
    ('INTERVIEW_FEEDBACK_READY', 'EMAIL', 'Your Interview Feedback is Ready',
     'Your interview practice session feedback is now available. Check your dashboard to review your performance.',
     '["session_url"]'),
    ('AUTOMATION_COMPLETED', 'IN_APP', NULL,
     'Application successfully submitted to {{company}} for {{job_title}}.',
     '["job_title", "company"]'),
    ('AUTOMATION_FAILED', 'EMAIL', 'Application Automation Failed',
     'We were unable to automatically submit your application to {{company}}. Please check the details and try manually.',
     '["job_title", "company", "error", "application_url"]');

-- Admin user (password: admin123)
INSERT INTO users (id, email, password_hash, name, role, tier, email_verified)
VALUES ('00000000-0000-0000-0000-000000000001',
        'admin@jobpilot.dev',
        '$2a$10$xVqYLjKlW7QFJ3YqZ0J7Ye8Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5',
        'Admin User', 'ADMIN', 'PRO', TRUE);

-- Demo user (password: demo1234)
INSERT INTO users (id, email, password_hash, name, role, tier, email_verified)
VALUES ('00000000-0000-0000-0000-000000000002',
        'demo@jobpilot.dev',
        '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkf9Rn6bm1FZwOJK2v2Lp7g5Y5Y5e',
        'Demo User', 'FREE', 'FREE', TRUE);

INSERT INTO user_settings (user_id, job_preferences, notification_prefs, privacy_settings, ai_preferences, appearance)
VALUES ('00000000-0000-0000-0000-000000000002',
    '{"desiredRoles": ["Software Engineer", "Backend Developer"], "preferredLocations": ["Remote"], "remotePreference": "REMOTE", "employmentTypes": ["FULL_TIME"]}',
    '{"emailDigest": "INSTANT", "inAppEnabled": true, "pushEnabled": false}',
    '{"profileVisibility": "PUBLIC"}',
    '{"preferredProvider": "openai"}',
    '{"theme": "system"}');
