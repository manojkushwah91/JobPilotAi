-- V22: Add mission tracking columns to application_results
ALTER TABLE application_results ADD COLUMN user_id VARCHAR(36);
ALTER TABLE application_results ADD COLUMN mission_id VARCHAR(36);
ALTER TABLE application_results ADD COLUMN job_title VARCHAR(500);
ALTER TABLE application_results ADD COLUMN company_name VARCHAR(200);
ALTER TABLE application_results ADD COLUMN applied_at TIMESTAMP;

CREATE INDEX idx_application_results_mission ON application_results(mission_id);
CREATE INDEX idx_application_results_user ON application_results(user_id);
