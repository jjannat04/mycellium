CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NULL,
    event_id BIGINT NULL,
    read_status BIT NULL DEFAULT 0,
    created_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_notifications_user_created (user_email, created_at),
    INDEX idx_notifications_user_read (user_email, read_status)
);
