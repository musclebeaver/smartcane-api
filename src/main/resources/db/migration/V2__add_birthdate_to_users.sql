-- V2__add_birthdate_to_users.sql
-- users 테이블에 생년월일 컬럼 추가 (이미 V1이 적용된 환경 대상)
ALTER TABLE users
  ADD COLUMN birthDate DATE NULL AFTER nickname;
