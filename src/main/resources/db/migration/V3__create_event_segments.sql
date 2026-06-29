CREATE TABLE event_segments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NULL,
    fee DECIMAL(10, 2) NULL,
    team_size INT NULL,
    capacity INT NULL,
    registration_link VARCHAR(255) NULL,
    registration_deadline DATETIME(6) NULL,
    created_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_event_segments_event_id (event_id)
);
