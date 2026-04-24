-- ============================================================
-- Product Inventory & Stock Management System
-- MySQL Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- -----------------------------------------------------------
-- 1. Products Table
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code    VARCHAR(50)    NOT NULL UNIQUE,
    name            VARCHAR(150)   NOT NULL,
    description     TEXT,
    price           DOUBLE         NOT NULL CHECK (price >= 0),
    total_stock     INT            NOT NULL DEFAULT 0,
    available_stock INT            NOT NULL DEFAULT 0,
    low_stock_threshold INT        NOT NULL DEFAULT 10,
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------
-- 2. Suppliers Table
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)   NOT NULL,
    email           VARCHAR(200)   NOT NULL,
    phone_number    VARCHAR(20)    NOT NULL,
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------
-- 3. Stock Transactions Table
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_transactions (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_code     VARCHAR(50)   NOT NULL UNIQUE,
    product_id         BIGINT        NOT NULL,
    quantity           INT           NOT NULL CHECK (quantity > 0),
    transaction_type   ENUM('IN','OUT') NOT NULL,
    transaction_date   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    status             ENUM('COMPLETED','CANCELLED') NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------
-- Indexes for performance
-- -----------------------------------------------------------
CREATE INDEX idx_product_code    ON products(product_code);
CREATE INDEX idx_product_name    ON products(name);
CREATE INDEX idx_txn_product     ON stock_transactions(product_id);
CREATE INDEX idx_txn_ref         ON stock_transactions(reference_code);
CREATE INDEX idx_txn_type        ON stock_transactions(transaction_type);
