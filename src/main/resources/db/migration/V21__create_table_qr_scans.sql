CREATE TABLE IF NOT EXISTS qr_scans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    scanned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    user_agent VARCHAR(512),
    ip VARCHAR(45),
    referer VARCHAR(512),
    PRIMARY KEY (id)
);

CREATE INDEX idx_qr_scans_code ON qr_scans(code);
CREATE INDEX idx_qr_scans_scanned_at ON qr_scans(scanned_at);
