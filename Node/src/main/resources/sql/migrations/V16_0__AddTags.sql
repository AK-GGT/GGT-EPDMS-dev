DROP TABLE IF EXISTS `tag`;
CREATE TABLE IF NOT EXISTS `tag` (
  `ID` BIGINT NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_tag`;
CREATE TABLE IF NOT EXISTS `process_tag` (
  process_ID BIGINT NOT NULL, 
  tag_ID BIGINT NOT NULL, 
  PRIMARY KEY(process_ID, tag_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_tag`;
CREATE TABLE IF NOT EXISTS `datastock_tag` (
  datastock_ID BIGINT NOT NULL, 
  tag_ID BIGINT NOT NULL, 
  PRIMARY KEY (datastock_ID, tag_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE process_tag ADD CONSTRAINT FK_process_tag_process_ID FOREIGN KEY (process_ID) REFERENCES process (ID) ON DELETE CASCADE;
ALTER TABLE process_tag ADD CONSTRAINT FK_process_tag_tag_ID FOREIGN KEY (tag_ID) REFERENCES tag (ID) ON DELETE CASCADE;
  
ALTER TABLE datastock_tag ADD CONSTRAINT FK_datastock_tag_datastock_ID FOREIGN KEY (datastock_ID) REFERENCES datastock (ID) ON DELETE CASCADE;
ALTER TABLE datastock_tag ADD CONSTRAINT FK_datastock_tag_tag_ID FOREIGN KEY (tag_ID) REFERENCES tag (ID) ON DELETE CASCADE;