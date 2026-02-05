CREATE TABLE IF NOT EXISTS user_terms_agreements
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    terms_version DECIMAL(2, 1) NOT NULL,
    agree_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip VARCHAR(64),
    user_device VARCHAR(256),

    CONSTRAINT __user_terms_agreements_fk_usre_id FOREIGN KEY (user_id) REFERENCES users(id)
);