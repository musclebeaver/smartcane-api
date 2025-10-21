-- ===========================================
-- Billing/Invoice/Payment/Ride/Webhook 테이블 생성 (MySQL 8)
-- ===========================================

-- 1) billing_profile
CREATE TABLE IF NOT EXISTS billing_profile (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT        NULL,
  customer_key   VARCHAR(64)   NOT NULL,
  billing_key    VARCHAR(128)  NULL,
  status         VARCHAR(16)   NULL,               -- ACTIVE | INACTIVE | REVOKED
  created_at     TIMESTAMP     NULL,
  updated_at     TIMESTAMP     NULL,

  CONSTRAINT uk_billing_customer_key UNIQUE (customer_key),
  KEY idx_billing_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) invoice
CREATE TABLE IF NOT EXISTS invoice (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT          NOT NULL,
  order_id     VARCHAR(120)    NOT NULL,           -- unique
  title        VARCHAR(200)    NULL,
  amount       DECIMAL(15,2)   NOT NULL,
  currency     VARCHAR(8)      NOT NULL,           -- KRW
  status       VARCHAR(16)     NOT NULL,           -- PENDING | PAYING | PAID | FAILED | CANCELED
  payment_key  VARCHAR(64)     NULL,
  billed_from  TIMESTAMP       NULL,
  billed_to    TIMESTAMP       NULL,
  created_at   TIMESTAMP       NULL,
  updated_at   TIMESTAMP       NULL,

  CONSTRAINT uk_invoice_order_id UNIQUE (order_id),
  KEY idx_invoice_user   (user_id),
  KEY idx_invoice_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) payment_ledger
CREATE TABLE IF NOT EXISTS payment_ledger (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_key    VARCHAR(64)     NULL,
  order_id       VARCHAR(100)    NOT NULL,         -- unique (정책에 따라 unique 해제 가능)
  order_name     VARCHAR(200)    NULL,
  amount         DECIMAL(15,2)   NOT NULL,
  currency       VARCHAR(8)      NOT NULL,
  method         VARCHAR(16)     NULL,             -- CARD / VIRTUAL_ACCOUNT / EASY_PAY ...
  issuer_code    VARCHAR(8)      NULL,
  acquirer_code  VARCHAR(8)      NULL,
  status         VARCHAR(32)     NULL,             -- READY / IN_PROGRESS / DONE / ...
  approved_at    TIMESTAMP       NULL,
  canceled_at    TIMESTAMP       NULL,
  created_at     TIMESTAMP       NULL,
  updated_at     TIMESTAMP       NULL,

  CONSTRAINT uk_pl_order_id UNIQUE (order_id),
  KEY idx_pl_payment_key (payment_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) ride_log
CREATE TABLE IF NOT EXISTS ride_log (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT         NOT NULL,
  device_id     VARCHAR(64)    NULL,
  mode          VARCHAR(10)    NOT NULL,           -- BUS | SUBWAY
  started_at    TIMESTAMP      NOT NULL,
  ended_at      TIMESTAMP      NOT NULL,
  distance_m    INT            NULL,
  route_id      VARCHAR(64)    NULL,
  route_name    VARCHAR(128)   NULL,
  origin_id     VARCHAR(64)    NULL,
  origin_name   VARCHAR(128)   NULL,
  dest_id       VARCHAR(64)    NULL,
  dest_name     VARCHAR(128)   NULL,

  KEY idx_ride_user_started    (user_id, started_at),
  KEY idx_ride_started         (started_at),
  KEY idx_ride_device_started  (device_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) webhook_receipt
CREATE TABLE IF NOT EXISTS webhook_receipt (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  provider         VARCHAR(16)   NOT NULL,         -- "TOSS"
  transmission_id  VARCHAR(128)  NOT NULL,
  event_type       VARCHAR(64)   NULL,
  received_at      TIMESTAMP     NOT NULL,

  CONSTRAINT uk_provider_transmission UNIQUE (provider, transmission_id),
  KEY idx_wr_received_at (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
