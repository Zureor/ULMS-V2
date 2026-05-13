-- ============================================
-- ULMS Database Schema
-- University Library Management System
-- MySQL 8.x
-- ============================================

CREATE DATABASE IF NOT EXISTS `ulms_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ulms_db`;

-- Users table
CREATE TABLE IF NOT EXISTS `users` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(50)     NOT NULL UNIQUE,
    `password`      VARCHAR(255)    NOT NULL,
    `full_name`     VARCHAR(100)    NOT NULL,
    `email`         VARCHAR(100)    NOT NULL UNIQUE,
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'STUDENT',
    `student_id`    VARCHAR(20)     UNIQUE,
    `department`    VARCHAR(100),
    `is_active`     BOOLEAN         NOT NULL DEFAULT TRUE,
    `created_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_student_id (student_id)
);

-- Books table
CREATE TABLE IF NOT EXISTS `books` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `isbn`              VARCHAR(20)     NOT NULL UNIQUE,
    `title`             VARCHAR(255)    NOT NULL,
    `author`            VARCHAR(255)    NOT NULL,
    `publisher`         VARCHAR(255),
    `edition`           VARCHAR(50),
    `category`          VARCHAR(100)    NOT NULL,
    `total_copies`      INT             NOT NULL DEFAULT 1,
    `available_copies`  INT             NOT NULL DEFAULT 1,
    `description`       TEXT,
    `cover_image_url`   VARCHAR(500),
    `added_by`          BIGINT,
    `created_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`added_by`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_author (author),
    INDEX idx_title (title)
);

-- Reservations table
CREATE TABLE IF NOT EXISTS `reservations` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`           BIGINT          NOT NULL,
    `book_id`           BIGINT          NOT NULL,
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    `issue_date`        DATE,
    `due_date`          DATE,
    `return_date`       DATE,
    `approved_by`       BIGINT,
    `rejection_note`    TEXT,
    `created_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`book_id`) REFERENCES `books`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`approved_by`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX idx_user_status (user_id, status),
    INDEX idx_book_status (book_id, status)
);

-- Fines table
CREATE TABLE IF NOT EXISTS `fines` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`           BIGINT          NOT NULL,
    `reservation_id`    BIGINT          NOT NULL,
    `amount`            DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    `reason`            VARCHAR(255)    NOT NULL DEFAULT 'OVERDUE',
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'UNPAID',
    `paid_at`           TIMESTAMP,
    `created_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`reservation_id`) REFERENCES `reservations`(`id`) ON DELETE CASCADE,
    INDEX idx_user_fines (user_id, status),
    INDEX idx_reservation (reservation_id)
);

-- Audit log table
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id`                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `action`            VARCHAR(50)     NOT NULL,
    `entity_type`       VARCHAR(50)     NOT NULL,
    `entity_id`         BIGINT,
    `performed_by`      BIGINT,
    `details`           TEXT,
    `created_at`        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`performed_by`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id)
);