CREATE TABLE IF NOT EXISTS chat_message
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pot_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    text TEXT,
    datetime_send_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT __chat_message_fk_pot_id FOREIGN KEY (pot_id) REFERENCES pots (id) ON DELETE CASCADE,
    CONSTRAINT __chat_message_fk_sender_id FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX __chat_message_idx_pot_id_datetime_send_at ON chat_message (pot_id, datetime_send_at)