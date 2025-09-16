-- V3__add_provider_id_to_user_auth.sql
-- user_auth에 소셜 고유 ID 저장을 위한 provider_id 컬럼 추가 및 인덱스 생성

ALTER TABLE user_auth
  ADD COLUMN provider_id VARCHAR(100) NULL AFTER provider;

-- (provider, provider_id) 조합으로 빠르게 조회
CREATE INDEX ix_userauth_provider_providerid ON user_auth (provider, provider_id);
