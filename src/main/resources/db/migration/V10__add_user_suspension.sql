-- 정지 풀리는 날짜 (NULL이면 정상 회원)
ALTER TABLE users ADD COLUMN suspended_until TIMESTAMP(6) NULL;

-- 누적 정지 횟수 (기본값 0)
ALTER TABLE users ADD COLUMN suspension_count INT NOT NULL DEFAULT 0;