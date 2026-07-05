-- Convert PostgreSQL custom enum columns to VARCHAR for H2 compatibility in tests
-- This allows @Enumerated(EnumType.STRING) to work with both PostgreSQL and H2

-- 1. Convert job_listings.employment_type from enum to VARCHAR
ALTER TABLE job_listings ALTER COLUMN employment_type TYPE VARCHAR(50);
ALTER TABLE job_listings ALTER COLUMN experience_level TYPE VARCHAR(50);

-- 2. Convert applications.status from enum to VARCHAR
ALTER TABLE applications ALTER COLUMN status TYPE VARCHAR(50);

-- 3. Convert users.role from enum to VARCHAR
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(50);

-- 4. Convert notifications columns if they exist
DO $$ BEGIN
    ALTER TABLE notifications ALTER COLUMN channel TYPE VARCHAR(50);
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$ BEGIN
    ALTER TABLE notifications ALTER COLUMN status TYPE VARCHAR(50);
EXCEPTION WHEN undefined_column THEN NULL;
END $$;

-- 5. Convert subscriptions.status if it exists
DO $$ BEGIN
    ALTER TABLE subscriptions ALTER COLUMN status TYPE VARCHAR(50);
EXCEPTION WHEN undefined_column THEN NULL;
END $$;

-- 6. Drop the enum types (safe since no columns reference them anymore)
DROP TYPE IF EXISTS employment_type CASCADE;
DROP TYPE IF EXISTS experience_level CASCADE;
DROP TYPE IF EXISTS application_status CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS notification_channel CASCADE;
DROP TYPE IF EXISTS notification_status CASCADE;
DROP TYPE IF EXISTS subscription_status CASCADE;
