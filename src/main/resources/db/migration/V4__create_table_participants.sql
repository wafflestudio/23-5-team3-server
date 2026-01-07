CREATE TABLE IF NOT EXISTS participants
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pot_id BIGINT NOT NULL,
    joined_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT __participants_fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT __participants_fk_pot_id FOREIGN KEY (pot_id) REFERENCES pots (id) ON DELETE CASCADE,

    UNIQUE KEY __participants_uk (user_id)
);