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

CREATE TABLE IF NOT EXISTS branches (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_branches_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cars (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    branch_id   BIGINT      NOT NULL,
    car_number  VARCHAR(50) NOT NULL,
    car_type    VARCHAR(50) NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_cars_car_number (car_number),
    INDEX idx_cars_type_branch (car_type, branch_id),
    CONSTRAINT fk_cars_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS branch_prices (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    branch_id       BIGINT         NOT NULL,
    car_type        VARCHAR(50)    NOT NULL,
    price_per_hour  DECIMAL(12, 2) NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_branch_prices_branch_type (branch_id, car_type),
    INDEX idx_branch_prices_type_price (car_type, price_per_hour),
    CONSTRAINT fk_branch_prices_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bookings (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    user_id          BIGINT         NOT NULL,
    car_id           BIGINT         NOT NULL,
    branch_id        BIGINT         NOT NULL,
    car_type         VARCHAR(50)    NOT NULL,
    start_time       DATETIME(6)    NOT NULL,
    end_time         DATETIME(6)    NOT NULL,
    price_per_hour   DECIMAL(12, 2) NOT NULL,
    total_price      DECIMAL(12, 2) NOT NULL,
    status           VARCHAR(50)    NOT NULL,
    idempotency_key  VARCHAR(255)   NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       DATETIME(6),
    updated_at       DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_bookings_idempotency_key (idempotency_key),
    INDEX idx_bookings_car_slot (car_id, status, start_time, end_time),
    INDEX idx_bookings_user (user_id),
    CONSTRAINT fk_bookings_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_bookings_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
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
