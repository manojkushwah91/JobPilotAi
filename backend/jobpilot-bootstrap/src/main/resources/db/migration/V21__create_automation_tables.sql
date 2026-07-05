-- V21: Create browser automation tables
CREATE TABLE browser_sessions (
    id VARCHAR(36) PRIMARY KEY,
    board_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    current_url VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

CREATE INDEX idx_browser_sessions_status ON browser_sessions(status);
CREATE INDEX idx_browser_sessions_board ON browser_sessions(board_name);
CREATE INDEX idx_browser_sessions_active ON browser_sessions(status) WHERE status = 'ACTIVE';

CREATE TABLE application_results (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    job_url VARCHAR(500) NOT NULL,
    outcome VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES browser_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_application_results_session ON application_results(session_id);
CREATE INDEX idx_application_results_outcome ON application_results(outcome);
