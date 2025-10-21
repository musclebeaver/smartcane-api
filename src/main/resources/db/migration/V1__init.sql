-- ===========================================
-- Flyway V1 초기 스키마 (MySQL 8)
-- ===========================================

-- (옵션) 로컬 편의: DB 생성/선택을 여기서 하지 않음 (운영 분리 권장)
 CREATE DATABASE IF NOT EXISTS smartcane CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE smartcane;

-- 공통: 문자셋
-- SET NAMES utf8mb4;

-- =========================
-- 1) 사용자(User) 도메인
-- =========================

CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(255) NOT NULL,
  nickname      VARCHAR(100) NULL,
  birth_date    DATE         NULL,          -- 👈 추가
  status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT ux_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 로그인 수단/연동(로컬 패스워드/카카오/네이버 등)
CREATE TABLE IF NOT EXISTS user_auth (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id            BIGINT       NOT NULL,
  provider           VARCHAR(16)  NOT NULL,  -- LOCAL | APPLE | KAKAO | NAVER
  provider_id        VARCHAR(100) NULL,      -- LOCAL일 땐 NULL
  password_hash      VARCHAR(100) NULL,      -- LOCAL일 때만 사용
  refresh_token_hash VARCHAR(128) NULL,      -- 리프레시 토큰 '해시' 저장
  revoked_at         TIMESTAMP    NULL,

  -- Auditable (공통)
  created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  KEY ix_userauth_user          (user_id),
  KEY ix_userauth_provider      (provider),
  KEY ix_userauth_refresh_hash  (refresh_token_hash),

  CONSTRAINT fk_user_auth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- API 클라이언트(외부 앱/내부 서비스 식별용)
CREATE TABLE IF NOT EXISTS api_client (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  name               VARCHAR(80)   NOT NULL,
  api_key_hash       VARCHAR(128)  NOT NULL,   -- API 키 '해시' 저장
  scopes             JSON          NULL,       -- JSON 배열 문자열
  rate_limit_per_min INT           NOT NULL DEFAULT 60,
  enabled            TINYINT(1)    NOT NULL DEFAULT 1,

  -- Auditable (공통)
  created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  KEY ix_apiclient_keyhash (api_key_hash),
  KEY ix_apiclient_enabled (enabled)

  -- 필요하면 유니크 권장:
  -- , CONSTRAINT ux_apiclient_keyhash UNIQUE (api_key_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 2) 포인트(Point) 도메인
-- =========================

-- 한 사용자 1계좌 가정 (여러 계좌가 필요하면 UNIQUE 제거하고 account_no 등 추가)
CREATE TABLE IF NOT EXISTS point_accounts (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT      NOT NULL,
  balance    BIGINT      NOT NULL DEFAULT 0,

  -- Auditable (프로젝트에서 사용하는 실 컬럼명에 맞추세요)
  created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT ux_point_accounts_user UNIQUE (user_id),
  CONSTRAINT fk_point_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- (선택) 포인트 원장/이력까지 필요하면 후속 버전에서 point_ledger 추가 권장
-- CREATE TABLE point_ledger (...);  -- V2__add_point_ledger.sql 로 분리 추천

-- =========================
-- 3) 멱등성(중복 방지)
-- =========================
CREATE TABLE IF NOT EXISTS idempotency (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  key_hash      VARCHAR(128) NOT NULL,        -- 요청 키(예: userId:action:payload) 해시
  status        VARCHAR(32)  NOT NULL,        -- STARTED | DONE | FAILED
  response_body MEDIUMTEXT   NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT ux_idempotency_key UNIQUE (key_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 4) 시드 데이터(필요 시)
-- =========================
-- INSERT INTO users(email, nickname) VALUES ('admin@local','admin') ON DUPLICATE KEY UPDATE email=email;
-- INSERT INTO user_auth(user_id, provider, password_hash) SELECT id, 'local', '$2a$10$...' FROM users WHERE email='admin@local' ON DUPLICATE KEY UPDATE user_id=user_id;
-- INSERT INTO point_account(user_id) SELECT id FROM users WHERE email='admin@local' ON DUPLICATE KEY UPDATE user_id=user_id;
