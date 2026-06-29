ALTER TABLE event_segments
    ADD COLUMN min_team_size INT NULL,
    ADD COLUMN max_team_size INT NULL;

UPDATE event_segments
SET min_team_size = 1
WHERE min_team_size IS NULL;

UPDATE event_segments
SET max_team_size = team_size
WHERE max_team_size IS NULL
  AND team_size IS NOT NULL;

ALTER TABLE registrations
    ADD COLUMN registration_scope VARCHAR(64) NULL;

UPDATE registrations
SET registration_scope = CASE
    WHEN segment_id IS NULL THEN 'EVENT'
    ELSE CONCAT('SEGMENT:', segment_id)
END
WHERE registration_scope IS NULL;

ALTER TABLE registrations
    ADD COLUMN active_registration_scope VARCHAR(64)
    GENERATED ALWAYS AS (
        CASE
            WHEN status = 'REGISTERED' THEN registration_scope
            ELSE NULL
        END
    ) STORED;

CREATE UNIQUE INDEX uq_registrations_active_scope
    ON registrations (event_id, student_email, active_registration_scope);
