-- PayFlow Database Initialization Script
-- This script will be executed when the PostgreSQL container starts for the first time

-- Create extensions that might be needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create additional schemas if needed
CREATE SCHEMA IF NOT EXISTS payflow;
CREATE SCHEMA IF NOT EXISTS audit;

-- Set default schema search path
ALTER DATABASE payflowdb SET search_path TO payflow, public;

-- Create a dedicated user for read-only access (useful for reporting/analytics)
CREATE USER payflow_readonly WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE payflowdb TO payflow_readonly;
GRANT USAGE ON SCHEMA payflow TO payflow_readonly;

-- Grant permissions to the main user
GRANT ALL PRIVILEGES ON DATABASE payflowdb TO payflow_user;
GRANT ALL PRIVILEGES ON SCHEMA payflow TO payflow_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA payflow TO payflow_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA payflow TO payflow_user;

-- Set up basic configuration
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

-- Select the configuration to reload it
SELECT pg_reload_conf();

-- Log the initialization
INSERT INTO payflow.system_log (message, created_at) 
VALUES ('PayFlow database initialized successfully', NOW())
ON CONFLICT DO NOTHING;