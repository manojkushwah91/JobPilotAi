CREATE TABLE IF NOT EXISTS resume_versions (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    job_url VARCHAR(500),
    job_title VARCHAR(255),
    company_name VARCHAR(255),
    tailored_content TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resume_versions_resume_id ON resume_versions(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_versions_user_id ON resume_versions(user_id);
CREATE INDEX IF NOT EXISTS idx_resume_versions_job_url ON resume_versions(job_url);
