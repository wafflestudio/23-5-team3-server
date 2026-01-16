CREATE TABLE IF NOT EXISTS pots
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    departure_id BIGINT NOT NULL,
    destination_id BIGINT NOT NULL,
    departure_time TIMESTAMP(6) NOT NULL,
    min_capacity TINYINT NOT NULL,
    max_capacity TINYINT NOT NULL,
    current_count TINYINT NOT NULL,
    status VARCHAR(16) NOT NULL,

    CONSTRAINT pot__fk_owner_id FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT pot__fk_departure_id FOREIGN KEY (departure_id) REFERENCES landmarks (id) ON DELETE CASCADE,
    CONSTRAINT pot__fk_destination_id FOREIGN KEY (destination_id) REFERENCES landmarks (id) ON DELETE CASCADE
);