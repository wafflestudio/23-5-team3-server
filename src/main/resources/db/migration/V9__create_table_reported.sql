CREATE TABLE IF NOT EXISTS reported(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_processed TINYINT(1) NOT NULL DEFAULT 0,
    reported_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reporter_user_id BIGINT NOT NULL,
    reporter_email VARCHAR(256) NOT NULL,
    reported_user_id BIGINT NOT NULL,
    reported_email VARCHAR(256) NOT NULL,
    reason VARCHAR(16) NOT NULL,
    messages LONGTEXT NOT NULL
);

CREATE INDEX __reported_idx_reported_at ON reported (reported_at)