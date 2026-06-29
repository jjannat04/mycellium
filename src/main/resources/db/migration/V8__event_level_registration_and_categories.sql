CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uq_categories_name (name)
);

INSERT INTO categories (name) VALUES
    ('Arts'),
    ('Business'),
    ('Culture'),
    ('Music'),
    ('Sports'),
    ('Workshop');

ALTER TABLE events
    ADD COLUMN series_name VARCHAR(255) NULL,
    ADD COLUMN fee DECIMAL(10, 2) NULL,
    ADD COLUMN min_team_size INT NULL,
    ADD COLUMN max_team_size INT NULL,
    ADD COLUMN capacity INT NULL;

ALTER TABLE registrations
    ADD COLUMN team_leader_name VARCHAR(255) NULL,
    ADD COLUMN team_leader_email VARCHAR(255) NULL,
    ADD COLUMN transaction_id VARCHAR(255) NULL;

CREATE TABLE registration_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    registration_id BIGINT NOT NULL,
    member_name VARCHAR(255) NULL,
    member_email VARCHAR(255) NULL,
    PRIMARY KEY (id),
    INDEX idx_registration_members_registration_id (registration_id)
);

CREATE INDEX idx_events_status_category ON events (status, category);
CREATE INDEX idx_events_series_name ON events (series_name);
