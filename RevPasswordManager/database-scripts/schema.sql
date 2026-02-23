-- ============================================
-- RevPassword Manager Database Schema
-- MySQL 8.0+
-- ============================================

-- Create database (if it doesn't exist)
CREATE DATABASE IF NOT EXISTS revpassword_db;

-- Use the database
USE revpassword_db;

-- ============================================
-- Table: users
-- Stores user account information
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed login password',
    master_password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed master password',
    security_question VARCHAR(255) NOT NULL,
    security_answer VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed security answer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: vault_entries
-- Stores encrypted password entries
-- ============================================
CREATE TABLE IF NOT EXISTS vault_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NULL,
    password TEXT NOT NULL COMMENT 'AES encrypted password',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_account (user_id, account_name),
    INDEX idx_user_id (user_id),
    INDEX idx_account_name (account_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: otp
-- Stores one-time passwords for verification
-- ============================================
CREATE TABLE IF NOT EXISTS otp (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    code VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expiry (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Sample Data (Optional - for testing)
-- ============================================

-- Note: The following are sample entries for testing purposes only.
-- In production, users should be created through the application.
-- Passwords shown here are for demonstration - they will be hashed by the application.

-- Sample user (password: password123, master: master123)
-- INSERT INTO users (name, email, password, master_password, security_question, security_answer)
-- VALUES (
--     'Demo User',
--     'demo@example.com',
--     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.CjATZu', -- BCrypt hash of 'password123'
--     '$2a$12$5Z8HnK6YDgJKvYLYz.aOPuN5Z5MxPVbQJqG8JQHfPqZvH5YqPQ6TS', -- BCrypt hash of 'master123'
--     'What is your favorite color?',
--     '$2a$12$kBXP5VpJ5QXqHYGPKXH8gOK5qVKYH8xPQ9J5QXP5VpJ5QXqHYGPKX'  -- BCrypt hash of 'blue'
-- );

-- ============================================
-- Cleanup old OTPs (Maintenance Query)
-- Run this periodically to remove expired OTPs
-- ============================================

-- DELETE FROM otp WHERE expiry_time < NOW();

-- ============================================
-- Verify Tables Created
-- ============================================

SHOW TABLES;

-- ============================================
-- View Table Structures
-- ============================================

-- DESCRIBE users;
-- DESCRIBE vault_entries;
-- DESCRIBE otp;

-- ============================================
-- Success Message
-- ============================================

SELECT 'Database schema created successfully!' AS message;
