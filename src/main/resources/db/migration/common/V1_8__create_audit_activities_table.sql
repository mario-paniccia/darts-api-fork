CREATE TABLE IF NOT EXISTS audit_activities (
  id                    INTEGER                    NOT NULL
, name                  CHARACTER VARYING          NOT NULL
, description           CHARACTER VARYING          NOT NULL
, CONSTRAINT audit_activities_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS audit_activities_seq;

ALTER TABLE audit ADD FOREIGN KEY (audit_activity_id) REFERENCES audit_activities(id);

INSERT INTO audit_activities VALUES(1, 'Move Courtroom', 'Move Courtroom');
INSERT INTO audit_activities VALUES(2, 'Export Audio', 'Export Audio');
INSERT INTO audit_activities VALUES(3, 'Request Audio', 'Request Audio');
INSERT INTO audit_activities VALUES(4, 'Audio Playback', 'Audio Playback');
INSERT INTO audit_activities VALUES(5, 'Apply Retention', 'Apply Retention');
INSERT INTO audit_activities VALUES(6, 'Request Transcription', 'Request Transcription');
INSERT INTO audit_activities VALUES(7, 'Import Transcription', 'Import Transcription');

ALTER SEQUENCE audit_activities_seq RESTART WITH 8;