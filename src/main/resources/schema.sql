-- ═══════════════════════════════════════════════════════════════════════
-- schema.sql — Slice Interview Boilerplate
-- Runs on every startup (spring.sql.init.mode=always).
-- Use IF NOT EXISTS so it's idempotent (safe to re-run).
-- ═══════════════════════════════════════════════════════════════════════

-- INTERVIEW TIP: Add your domain-specific tables BELOW the items table.
-- Keep this file. During the interview, just add new CREATE TABLE blocks.

-- ── Sample: items ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS items (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(1000),
    price       DECIMAL(10, 2),
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED'),
    version     BIGINT          NOT NULL DEFAULT 0,  -- optimistic lock column
    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY  uk_items_name   (name),
    INDEX       idx_items_status (status),
    INDEX       idx_items_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ══════════════════════════════════════════════════════════════════════
-- INTERVIEW EXTENSION AREA
-- Add your problem-specific tables here during the interview.
-- ══════════════════════════════════════════════════════════════════════

-- Example 1: Notification System
-- CREATE TABLE IF NOT EXISTS notifications (
--     id               BIGINT       NOT NULL AUTO_INCREMENT,
--     idempotency_key  VARCHAR(255) NOT NULL,   -- deduplication
--     user_id          BIGINT       NOT NULL,
--     channel          VARCHAR(50)  NOT NULL,   -- EMAIL | SMS | PUSH
--     payload          JSON,
--     status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
--     retry_count      INT          NOT NULL DEFAULT 0,
--     scheduled_at     DATETIME(6),
--     sent_at          DATETIME(6),
--     created_at       DATETIME(6),
--     PRIMARY KEY (id),
--     UNIQUE KEY uk_notifications_idempotency (idempotency_key),
--     INDEX idx_notifications_user (user_id),
--     INDEX idx_notifications_status (status)
-- ) ENGINE=InnoDB;

-- Example 2: Wallet / Ledger
-- CREATE TABLE IF NOT EXISTS wallets (
--     id         BIGINT        NOT NULL AUTO_INCREMENT,
--     user_id    BIGINT        NOT NULL UNIQUE,
--     balance    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
--     version    BIGINT        NOT NULL DEFAULT 0,   -- optimistic lock
--     created_at DATETIME(6),
--     updated_at DATETIME(6),
--     PRIMARY KEY (id)
-- ) ENGINE=InnoDB;
--
-- CREATE TABLE IF NOT EXISTS transactions (
--     id             BIGINT        NOT NULL AUTO_INCREMENT,
--     wallet_id      BIGINT        NOT NULL,
--     type           VARCHAR(50)   NOT NULL,  -- CREDIT | DEBIT
--     amount         DECIMAL(15,2) NOT NULL,
--     reference_id   VARCHAR(255),
--     status         VARCHAR(50)   NOT NULL DEFAULT 'SUCCESS',
--     created_at     DATETIME(6),
--     PRIMARY KEY (id),
--     INDEX idx_transactions_wallet (wallet_id),
--     FOREIGN KEY (wallet_id) REFERENCES wallets(id)
-- ) ENGINE=InnoDB;

-- Example 3: Parking Lot
-- CREATE TABLE IF NOT EXISTS parking_slots (
--     id           BIGINT      NOT NULL AUTO_INCREMENT,
--     slot_number  VARCHAR(50) NOT NULL,
--     floor        INT         NOT NULL DEFAULT 0,
--     type         VARCHAR(50) NOT NULL,   -- COMPACT | REGULAR | LARGE
--     status       VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
--     version      BIGINT      NOT NULL DEFAULT 0,
--     PRIMARY KEY (id),
--     UNIQUE KEY uk_slot_number (slot_number),
--     INDEX idx_slot_status_type (status, type)
-- ) ENGINE=InnoDB;
