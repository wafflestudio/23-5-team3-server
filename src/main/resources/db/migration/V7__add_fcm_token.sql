CREATE TABLE IF NOT EXISTS user_devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    browser_type VARCHAR(32),
    device_id VARCHAR(128),
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_user_devices_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    );