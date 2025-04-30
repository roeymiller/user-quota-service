-- Create tables only if they don't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

-- Create quota_resource table
CREATE TABLE IF NOT EXISTS quota_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocking_threshold INT NOT NULL,
    resource_name VARCHAR(255) NOT NULL UNIQUE
);

-- Create quota_resource_one table
CREATE TABLE IF NOT EXISTS quota_resource_one (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES quota_resource(id)
);

-- Create quota_resource_two table
CREATE TABLE IF NOT EXISTS quota_resource_two (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES quota_resource(id)
);

-- Create blocked_users table
CREATE TABLE IF NOT EXISTS blocked_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    api_name VARCHAR(255) NOT NULL,
    blocked_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes - checking if they exist first
SET @exist_idx_user_id := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_name = 'blocked_users' AND index_name = 'idx_user_id' AND table_schema = DATABASE());
SET @exist_idx_api_name := (SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_name = 'blocked_users' AND index_name = 'idx_api_name' AND table_schema = DATABASE());

SET @create_idx_user_id = IF(@exist_idx_user_id = 0, 'CREATE INDEX idx_user_id ON blocked_users (user_id)', 'SELECT 1');
SET @create_idx_api_name = IF(@exist_idx_api_name = 0, 'CREATE INDEX idx_api_name ON blocked_users (api_name)', 'SELECT 1');

PREPARE stmt_user_id FROM @create_idx_user_id;
PREPARE stmt_api_name FROM @create_idx_api_name;

EXECUTE stmt_user_id;
EXECUTE stmt_api_name;

DEALLOCATE PREPARE stmt_user_id;
DEALLOCATE PREPARE stmt_api_name; 