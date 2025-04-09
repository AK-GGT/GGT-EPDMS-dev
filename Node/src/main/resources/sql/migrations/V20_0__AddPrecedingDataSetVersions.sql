DROP TABLE IF EXISTS `process_preceding_versions`;
CREATE TABLE IF NOT EXISTS `process_preceding_versions` (
  process_ID BIGINT NOT NULL, 
  reference_ID BIGINT NOT NULL, 
  PRIMARY KEY(process_ID, reference_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE process_preceding_versions ADD CONSTRAINT FK_process_preceding_versions_process_ID FOREIGN KEY (process_ID) REFERENCES process (ID) ON DELETE CASCADE;
ALTER TABLE process_preceding_versions ADD CONSTRAINT FK_process_preceding_versions_reference_ID FOREIGN KEY (reference_ID) REFERENCES globalreference (ID) ON DELETE CASCADE;