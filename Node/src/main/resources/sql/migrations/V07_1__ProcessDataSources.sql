CREATE TABLE process_datasource (process_ID BIGINT NOT NULL, datasource_ID BIGINT NOT NULL, PRIMARY KEY (process_ID, datasource_ID));
ALTER TABLE process_datasource ADD CONSTRAINT FK_process_datasource_datasource_ID FOREIGN KEY (datasource_ID) REFERENCES globalreference (ID) ON DELETE CASCADE;
ALTER TABLE process_datasource ADD CONSTRAINT FK_process_datasource_process_ID FOREIGN KEY (process_ID) REFERENCES process (ID) ON DELETE CASCADE;
