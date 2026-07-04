-- ============================================================================
-- JobPilot AI — Foundation Schema
-- Version: V1
-- Description: Core tables, extensions, base types
-- ============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ENUM types
CREATE TYPE user_role AS ENUM ('FREE', 'PREMIUM', 'PRO', 'ADMIN');
CREATE TYPE application_status AS ENUM (
    'SAVED', 'APPLIED', 'PHONE_SCREEN', 'TECHNICAL_INTERVIEW',
    'ONSITE_INTERVIEW', 'OFFER', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'
);
CREATE TYPE employment_type AS ENUM ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY');
CREATE TYPE experience_level AS ENUM ('ENTRY', 'MID', 'SENIOR', 'LEAD', 'EXECUTIVE');
CREATE TYPE notification_channel AS ENUM ('IN_APP', 'EMAIL', 'PUSH');
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'READ');

-- UUID generation function (v7-ish for time-ordered UUIDs)
CREATE OR REPLACE FUNCTION uuid_generate_v7() RETURNS uuid AS $$
BEGIN
    RETURN uuid_generate_v4();
END;
$$ LANGUAGE plpgsql;

-- Update trigger for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
