-- V1__init.sql
-- SmartCane: Identity & Device 스키마 초기화
-- MySQL 8.x / InnoDB / utf8mb4

-- 공통 설정(옵션)
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- users
-- role: USER / ADMIN
-- status: ACTIVE / SUSPENDED / DELETED
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email             VARCHAR(190) NOT NULL,
  nickname          VARCHAR(60),
  role              VARCHAR(20) NOT NULL DEFAULT 'USER',
  status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY ux_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- user_auth
-- provider: LOCAL / APPLE / KAKAO / NAVER
-- refresh_token_hash: 보안상 해시로 저장
-- =========================================================
CREATE TABLE IF NOT EXISTS user_auth (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id              BIGINT UNSIGNED NOT NULL,
  provider             VARCHAR(16) NOT NULL DEFAULT 'LOCAL',
  password_hash        VARCHAR(100),
  refreshTokenHash     VARCHAR(128),
  revokedAt            DATETIME(6),
  created_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_userauth_user (user_id),
  KEY ix_userauth_provider (provider),
  KEY ix_userauth_refresh_hash (refreshTokenHash),
  CONSTRAINT fk_userauth_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- api_client
-- 외부/내부 시스템용 API Key (DB에는 해시만 저장)
-- scopes: JSON 배열(예: ["nav.read","media.write"])
-- =========================================================
CREATE TABLE IF NOT EXISTS api_client (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name                 VARCHAR(80) NOT NULL,
  apiKeyHash           VARCHAR(128) NOT NULL,
  scopes               JSON,
  rateLimitPerMin      INT NOT NULL DEFAULT 60,
  enabled              TINYINT(1) NOT NULL DEFAULT 1,
  created_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_apiclient_keyhash (apiKeyHash),
  KEY ix_apiclient_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- device
-- status: ACTIVE / INACTIVE / LOST
-- serial: 단말 시리얼(유니크)
-- =========================================================
CREATE TABLE IF NOT EXISTS device (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  serial            VARCHAR(64) NOT NULL,
  model             VARCHAR(60),
  firmware          VARCHAR(40),
  status            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  lastSeenAt        DATETIME(6),
  created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY ux_device_serial (serial),
  KEY ix_device_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- device_binding
-- 사용자-단말 바인딩 이력(활성/해제)
-- =========================================================
CREATE TABLE IF NOT EXISTS device_binding (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL,
  device_id         BIGINT UNSIGNED NOT NULL,
  boundAt           DATETIME(6),
  active            TINYINT(1) NOT NULL DEFAULT 1,
  created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_binding_user (user_id),
  KEY ix_binding_device (device_id),
  KEY ix_binding_active (active),
  CONSTRAINT fk_binding_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_binding_device
    FOREIGN KEY (device_id) REFERENCES device(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- device_key
-- 단말 공개키(JWK) / KID 기반 롤테이션
-- =========================================================
CREATE TABLE IF NOT EXISTS device_key (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  device_id         BIGINT UNSIGNED NOT NULL,
  kid               VARCHAR(64) NOT NULL,
  publicJwk         JSON NOT NULL,
  rotatedAt         DATETIME(6),
  created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_devicekey_device (device_id),
  KEY ix_devicekey_kid (kid),
  CONSTRAINT fk_devicekey_device
    FOREIGN KEY (device_id) REFERENCES device(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- offline_token
-- 오프라인 결제/승차 토큰 수명주기 (JTI 유니크)
-- status: VALID / REVOKED / USED
-- scope: JSON (예: {"type":"fare","limit":2500})
-- =========================================================
CREATE TABLE IF NOT EXISTS offline_token (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NULL,
  device_id         BIGINT UNSIGNED NOT NULL,
  jti               VARCHAR(64) NOT NULL,
  issuedAt          DATETIME(6),
  expiresAt         DATETIME(6),
  status            VARCHAR(12) NOT NULL DEFAULT 'VALID',
  audience          VARCHAR(64),
  scope             JSON,
  created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY ux_offtoken_jti (jti),
  KEY ix_offtoken_device (device_id),
  KEY ix_offtoken_expires (expiresAt),
  CONSTRAINT fk_offtoken_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT fk_offtoken_device
    FOREIGN KEY (device_id) REFERENCES device(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- location_snapshot
-- 단말(또는 사용자)의 시점별 위치 로그
-- =========================================================
CREATE TABLE IF NOT EXISTS location_snapshot (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NULL,
  device_id         BIGINT UNSIGNED NOT NULL,
  lat               DECIMAL(9,6) NOT NULL,
  lng               DECIMAL(9,6) NOT NULL,
  accuracyM         DECIMAL(6,2),
  capturedAt        DATETIME(6) NOT NULL,
  created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_locsnap_device (device_id),
  KEY ix_locsnap_capturedAt (capturedAt),
  KEY ix_locsnap_latlng (lat, lng),
  CONSTRAINT fk_locsnap_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT fk_locsnap_device
    FOREIGN KEY (device_id) REFERENCES device(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
