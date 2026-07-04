-- V20__create_agent_runtime_tables.sql
-- Agent Runtime domain tables for the Autonomous AI Job Agent

-- Missions table
CREATE TABLE IF NOT EXISTS missions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    target_role VARCHAR(255) NOT NULL,
    target_location VARCHAR(255),
    salary_min INTEGER,
    salary_max INTEGER,
    currency VARCHAR(10),
    preferred_companies TEXT,
    avoid_companies TEXT,
    preferred_skills TEXT,
    experience_level VARCHAR(50),
    employment_type VARCHAR(50),
    daily_application_limit INTEGER DEFAULT 20,
    deadline_days INTEGER DEFAULT 90,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    deadline_at TIMESTAMP,
    total_jobs_found INTEGER DEFAULT 0,
    total_applications_submitted INTEGER DEFAULT 0,
    total_rejected INTEGER DEFAULT 0,
    total_pending INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Agent Tasks table
CREATE TABLE IF NOT EXISTS agent_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID NOT NULL,
    user_id UUID NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INTEGER DEFAULT 5,
    description TEXT,
    input JSONB,
    output JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Agent Memories table
CREATE TABLE IF NOT EXISTS agent_memories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    memory_type VARCHAR(50) NOT NULL,
    memory_key VARCHAR(255) NOT NULL,
    value TEXT,
    metadata JSONB,
    confidence DOUBLE PRECISION DEFAULT 1.0,
    access_count INTEGER DEFAULT 0,
    last_accessed_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Agent Observations table
CREATE TABLE IF NOT EXISTS agent_observations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID NOT NULL,
    task_id UUID,
    observation_type VARCHAR(50) NOT NULL,
    source VARCHAR(100),
    description TEXT,
    data JSONB,
    relevance_score DOUBLE PRECISION DEFAULT 0.5,
    actionable BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Agent Decisions table
CREATE TABLE IF NOT EXISTS agent_decisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID NOT NULL,
    task_id UUID,
    decision_type VARCHAR(50) NOT NULL,
    reasoning TEXT,
    context JSONB,
    decision JSONB,
    confidence DOUBLE PRECISION DEFAULT 0.5,
    executed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_missions_user_id ON missions(user_id);
CREATE INDEX IF NOT EXISTS idx_missions_status ON missions(status);
CREATE INDEX IF NOT EXISTS idx_agent_tasks_mission_id ON agent_tasks(mission_id);
CREATE INDEX IF NOT EXISTS idx_agent_tasks_status ON agent_tasks(status);
CREATE INDEX IF NOT EXISTS idx_agent_tasks_priority ON agent_tasks(priority DESC, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_agent_memories_user_id ON agent_memories(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_memories_user_type ON agent_memories(user_id, memory_type);
CREATE INDEX IF NOT EXISTS idx_agent_memories_user_type_key ON agent_memories(user_id, memory_type, memory_key);
CREATE INDEX IF NOT EXISTS idx_agent_observations_mission_id ON agent_observations(mission_id);
CREATE INDEX IF NOT EXISTS idx_agent_decisions_mission_id ON agent_decisions(mission_id);
CREATE INDEX IF NOT EXISTS idx_agent_decisions_unexecuted ON agent_decisions(mission_id, executed);
