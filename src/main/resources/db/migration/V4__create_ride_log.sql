CREATE TABLE IF NOT EXISTS ride_log (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT        NOT NULL,
  device_id    VARCHAR(64)   NULL,
  mode         VARCHAR(10)   NOT NULL,     -- BUS | SUBWAY
  started_at   DATETIME(3)   NOT NULL,
  ended_at     DATETIME(3)   NOT NULL,
  distance_m   INT           NULL,

  route_id     VARCHAR(64)   NULL,
  route_name   VARCHAR(128)  NULL,
  origin_id    VARCHAR(64)   NULL,
  origin_name  VARCHAR(128)  NULL,
  dest_id      VARCHAR(64)   NULL,
  dest_name    VARCHAR(128)  NULL,

  KEY idx_ride_user_started   (user_id, started_at),
  KEY idx_ride_started        (started_at),
  KEY idx_ride_device_started (device_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
