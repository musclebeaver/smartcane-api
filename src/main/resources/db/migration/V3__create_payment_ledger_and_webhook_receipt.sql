-- ✅ 공통 설정 (필요 시)
-- SET sql_mode = 'STRICT_ALL_TABLES';

-- =====================================================================
-- 1) webhook_receipt : 웹훅 중복 처리용 수신 기록
--    - UNIQUE(provider, transmission_id) 로 중복 차단
-- =====================================================================
CREATE TABLE IF NOT EXISTS webhook_receipt (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  provider         VARCHAR(16)   NOT NULL COMMENT '예: TOSS',
  transmission_id  VARCHAR(128)  NOT NULL COMMENT 'tosspayments-webhook-transmission-id 또는 대체키',
  event_type       VARCHAR(64)            COMMENT 'payment.approved / payment.canceled / billingKey.deleted ...',
  received_at      DATETIME(3)    NOT NULL COMMENT '수신 시각(서버 기준)',

  CONSTRAINT uk_webhook_receipt_provider_txid
    UNIQUE (provider, transmission_id)
)
ENGINE=InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX IF NOT EXISTS idx_webhook_receipt_received_at
  ON webhook_receipt (received_at);

-- =====================================================================
-- 2) payment_ledger : 결제/환불(부분취소 포함) 정산 원장
--    - order_id UNIQUE로 멱등 보장(정책에 따라 해제 가능)
--    - 상태(status)는 VARCHAR로 관리(DONE, PARTIAL_CANCELED, CANCELED 등)
-- =====================================================================
CREATE TABLE IF NOT EXISTS payment_ledger (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_key    VARCHAR(64)              COMMENT 'PG paymentKey',
  order_id       VARCHAR(100)   NOT NULL  COMMENT '멱등키(보통 주문 ID)',
  order_name     VARCHAR(200)             COMMENT '예: 교통요금(YYYY-MM-DD ~ YYYY-MM-DD)',

  amount         DECIMAL(15,2)  NOT NULL DEFAULT 0 COMMENT '승인 금액(원가 기준)',
  currency       VARCHAR(8)     NOT NULL DEFAULT 'KRW',

  method         VARCHAR(16)              COMMENT 'CARD / VIRTUAL_ACCOUNT / EASY_PAY ...',
  issuer_code    VARCHAR(8)               COMMENT '발급사 코드',
  acquirer_code  VARCHAR(8)               COMMENT '매입사 코드',

  status         VARCHAR(32)              COMMENT 'READY, IN_PROGRESS, DONE, PARTIAL_CANCELED, CANCELED, ABORTED, EXPIRED',
  approved_at    DATETIME(3)              COMMENT '승인 시각',
  canceled_at    DATETIME(3)              COMMENT '최종 취소 시각',

  created_at     DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at     DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

  CONSTRAINT uk_payment_ledger_order_id UNIQUE (order_id)
)
ENGINE=InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX IF NOT EXISTS idx_payment_ledger_payment_key ON payment_ledger (payment_key);
