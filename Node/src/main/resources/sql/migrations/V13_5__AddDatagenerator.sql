CREATE TABLE process_datasetgenerator (process_ID BIGINT NOT NULL, contact_ID BIGINT NOT NULL, PRIMARY KEY (process_ID, contact_ID));
ALTER TABLE process_datasetgenerator ADD CONSTRAINT FK_process_datasetgenerator_contact_ID FOREIGN KEY (contact_ID) REFERENCES globalreference (ID) ON DELETE CASCADE;
ALTER TABLE process_datasetgenerator ADD CONSTRAINT FK_process_datasetgenerator_process_ID FOREIGN KEY (process_ID) REFERENCES process (ID) ON DELETE CASCADE;
