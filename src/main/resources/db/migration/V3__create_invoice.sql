CREATE TABLE IF NOT EXISTS invoice (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT       NOT NULL,
  order_id     VARCHAR(120) NOT NULL,
  title        VARCHAR(200),
  amount       DECIMAL(15,2) NOT NULL,
  currency     VARCHAR(8)    NOT NULL DEFAULT 'KRW',
  status       VARCHAR(16)   NOT NULL,
  payment_key  VARCHAR(64),

  billed_from  DATETIME(3) NULL,
  billed_to    DATETIME(3) NULL,

  created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

  CONSTRAINT uk_invoice_order_id UNIQUE (order_id),
  KEY idx_invoice_user (user_id),
  KEY idx_invoice_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
