-- webhook_receipt
CREATE TABLE IF NOT EXISTS webhook_receipt (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  provider VARCHAR(16) NOT NULL,
  transmission_id VARCHAR(128) NOT NULL,
  event_type VARCHAR(64),
  received_at TIMESTAMP NOT NULL,
  CONSTRAINT uk_provider_transmission UNIQUE (provider, transmission_id)
);

CREATE INDEX idx_wr_received_at ON webhook_receipt (received_at);

-- payment_ledger
CREATE TABLE IF NOT EXISTS payment_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_key VARCHAR(64),
  order_id VARCHAR(100) NOT NULL,
  order_name VARCHAR(200),
  amount DECIMAL(15,2) NOT NULL DEFAULT 0,
  currency VARCHAR(8) NOT NULL,
  method VARCHAR(16),
  issuer_code VARCHAR(8),
  acquirer_code VARCHAR(8),
  status VARCHAR(32),
  approved_at TIMESTAMP NULL,
  canceled_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_pl_order_id UNIQUE (order_id)
);

CREATE INDEX idx_pl_payment_key ON payment_ledger (payment_key);
