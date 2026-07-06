-- V25: Create candidate_profiles table for agent auto-apply
CREATE TABLE candidate_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    location VARCHAR(255),
    headline VARCHAR(500),
    summary TEXT,
    skills TEXT,
    experience TEXT,
    education TEXT,
    certifications TEXT,
    resume_text TEXT,
    resume_file_url VARCHAR(500),
    linkedin_url VARCHAR(500),
    portfolio_url VARCHAR(500),
    years_experience INTEGER,
    desired_role VARCHAR(255),
    desired_location VARCHAR(255),
    salary_expectation_min INTEGER,
    salary_expectation_max INTEGER,
    currency VARCHAR(10),
    employment_type VARCHAR(50),
    work_preference VARCHAR(50),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_candidate_profiles_user_id ON candidate_profiles(user_id);
