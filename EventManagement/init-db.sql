DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'event_management_db') THEN
        CREATE DATABASE event_management_db;
    END IF;
END $$;

CREATE USER new_user WITH PASSWORD 'new_password';
GRANT ALL PRIVILEGES ON DATABASE event_management_db TO new_user;
