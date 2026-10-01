CREATE TABLE IF NOT EXISTS outbox_event (
    event_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    event_type VARCHAR(32) NOT NULL,
    payload JSON NOT NULL,
    trace_id VARCHAR(128),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    available_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    lease_token CHAR(36) CHARACTER SET ascii COLLATE ascii_bin,
    lease_until DATETIME(6),
    last_error TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sent_at DATETIME(6),
    INDEX outbox_due (status, available_at),
    INDEX outbox_lease (status, lease_until),
    CHECK (status IN ('PENDING', 'PROCESSING', 'SENT')),
    CHECK (event_type IN ('NOTIFICATION', 'WELCOME_EMAIL'))
) ENGINE=InnoDB;
