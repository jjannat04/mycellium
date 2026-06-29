CREATE TABLE event_timelines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NULL,
    timeline_date DATETIME(6) NULL,
    display_order INT NULL,
    PRIMARY KEY (id),
    INDEX idx_event_timelines_event_id (event_id)
);
