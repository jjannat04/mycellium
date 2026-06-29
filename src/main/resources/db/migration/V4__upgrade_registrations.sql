ALTER TABLE registrations
    ADD COLUMN segment_id BIGINT NULL,
    ADD COLUMN team_name VARCHAR(255) NULL,
    ADD COLUMN team_size INT NULL,
    ADD COLUMN status VARCHAR(50) NULL DEFAULT 'REGISTERED';

UPDATE registrations SET status = 'REGISTERED' WHERE status IS NULL;

CREATE INDEX idx_registrations_event_status ON registrations (event_id, status);
CREATE INDEX idx_registrations_segment_status ON registrations (segment_id, status);
CREATE INDEX idx_registrations_student_status ON registrations (student_email, status);
