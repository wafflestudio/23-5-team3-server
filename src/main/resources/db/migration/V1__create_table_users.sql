CREATE TABLE IF NOT EXISTS users
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(64) NOT NULL,
    username VARCHAR(32) NOT NULL,
    profile_image_url TEXT,
    `role` VARCHAR(16) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    active_pot_id BIGINT
);

INSERT INTO users (email, username, profile_image_url, role, created_at, updated_at, active_pot_id) VALUES
('abcd@snu.ac.kr', 'abcduser', 'http', 'USER', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 1);