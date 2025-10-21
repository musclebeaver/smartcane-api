-- ===========================================
-- Device / DeviceBinding / DeviceKey / OfflineToken
-- UUID -> BINARY(16), Instant -> TIMESTAMP
-- ===========================================

-- 1) device
CREATE TABLE IF NOT EXISTS device (
  id            BINARY(16)    NOT NULL PRIMARY KEY,
  serial_no     VARCHAR(64)   NOT NULL,
  display_name  VARCHAR(128)  NULL,
  status        VARCHAR(16)   NOT NULL,                 -- ACTIVE | SUSPENDED | RETIRED
  created_at    TIMESTAMP     NOT NULL,
  updated_at    TIMESTAMP     NOT NULL,

  CONSTRAINT ux_device_sn UNIQUE (serial_no),
  KEY ix_device_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) device_binding
CREATE TABLE IF NOT EXISTS device_binding (
  id         BINARY(16)   NOT NULL PRIMARY KEY,
  device_id  BINARY(16)   NOT NULL,
  user_id    BINARY(16)   NOT NULL,
  active     TINYINT(1)   NOT NULL,                      -- boolean
  bound_at   TIMESTAMP    NOT NULL,
  unbound_at TIMESTAMP    NULL,

  KEY ix_binding_user (user_id),
  KEY ix_binding_device_active (device_id, active),
  CONSTRAINT fk_binding_device FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE
  -- NOTE: user_id는 별도 users(UUID) 테이블이 있다면 아래 FK를 추가해도 됩니다.
  -- , CONSTRAINT fk_binding_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) device_key
CREATE TABLE IF NOT EXISTS device_key (
  id               BINARY(16)   NOT NULL PRIMARY KEY,
  device_id        BINARY(16)   NOT NULL,
  kid              VARCHAR(64)  NOT NULL,
  jwk_private_json LONGTEXT     NOT NULL,                 -- @Lob
  jwk_public_json  LONGTEXT     NOT NULL,                 -- @Lob
  algorithm        VARCHAR(16)  NOT NULL,                 -- ED25519 / ES256
  active           TINYINT(1)   NOT NULL,                 -- boolean
  created_at       TIMESTAMP    NOT NULL,

  CONSTRAINT ux_key_kid UNIQUE (kid),
  KEY ix_key_device (device_id),
  CONSTRAINT fk_key_device FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) offline_token
CREATE TABLE IF NOT EXISTS offline_token (
  id         BINARY(16)   NOT NULL PRIMARY KEY,
  device_id  BINARY(16)   NOT NULL,
  token      LONGTEXT     NOT NULL,                       -- @Lob (JWS compact)
  scope      VARCHAR(64)  NULL,
  issued_at  TIMESTAMP    NOT NULL,
  expires_at TIMESTAMP    NOT NULL,
  revoked    TINYINT(1)   NOT NULL,                       -- boolean

  KEY ix_offline_device (device_id),
  KEY ix_offline_exp (expires_at),
  CONSTRAINT fk_offline_device FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
