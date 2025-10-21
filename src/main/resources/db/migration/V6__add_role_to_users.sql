ALTER TABLE users
  ADD COLUMN role VARCHAR(16) NULL AFTER nickname;

-- 프로젝트 기본값이 USER라면 기본 채우고 NOT NULL 승격
UPDATE users SET role = COALESCE(role, 'USER');

ALTER TABLE users
  MODIFY role VARCHAR(16) NOT NULL;
