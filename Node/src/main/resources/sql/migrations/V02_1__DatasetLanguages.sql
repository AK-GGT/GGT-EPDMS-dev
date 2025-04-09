CREATE TABLE IF NOT EXISTS `contact_languages` (
	Contact_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (Contact_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `contact_languages` ADD CONSTRAINT FK_contact_languages_Contact_ID FOREIGN KEY (Contact_ID) REFERENCES contact (ID);
ALTER TABLE `contact_languages` ADD CONSTRAINT FK_contact_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);

CREATE TABLE IF NOT EXISTS `flow_common_languages` (
	Flow_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (Flow_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `flow_common_languages` ADD CONSTRAINT FK_flow_common_languages_Flow_ID FOREIGN KEY (Flow_ID) REFERENCES flow_common (ID);
ALTER TABLE `flow_common_languages` ADD CONSTRAINT FK_flow_common_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);

CREATE TABLE IF NOT EXISTS `flowproperty_languages` (
	FlowProperty_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (FlowProperty_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `flowproperty_languages` ADD CONSTRAINT FK_flowproperty_languages_FlowProperty_ID FOREIGN KEY (FlowProperty_ID) REFERENCES flowproperty (ID);
ALTER TABLE `flowproperty_languages` ADD CONSTRAINT FK_flowproperty_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);

CREATE TABLE IF NOT EXISTS `lciamethod_languages` (
	LCIAMethod_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (LCIAMethod_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `lciamethod_languages` ADD CONSTRAINT FK_lciamethod_languages_LCIAMethod_ID FOREIGN KEY (LCIAMethod_ID) REFERENCES lciamethod (ID);
ALTER TABLE `lciamethod_languages` ADD CONSTRAINT FK_lciamethod_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);

CREATE TABLE IF NOT EXISTS `process_languages` (
	Process_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (Process_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `process_languages` ADD CONSTRAINT FK_process_languages_Process_ID FOREIGN KEY (Process_ID) REFERENCES process (ID);
ALTER TABLE `process_languages` ADD CONSTRAINT FK_process_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);

CREATE TABLE IF NOT EXISTS `source_languages` (
	Source_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (Source_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `source_languages` ADD CONSTRAINT FK_source_languages_Source_ID FOREIGN KEY (Source_ID) REFERENCES source (ID);
ALTER TABLE `source_languages` ADD CONSTRAINT FK_source_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);

CREATE TABLE IF NOT EXISTS `unitgroup_languages` (
	UnitGroup_ID BIGINT NOT NULL, 
	supportedLanguages_ID BIGINT NOT NULL, 
	PRIMARY KEY (UnitGroup_ID, supportedLanguages_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `unitgroup_languages` ADD CONSTRAINT FK_unitgroup_languages_supportedLanguages_ID FOREIGN KEY (supportedLanguages_ID) REFERENCES languages (ID);
ALTER TABLE `unitgroup_languages` ADD CONSTRAINT FK_unitgroup_languages_UnitGroup_ID FOREIGN KEY (UnitGroup_ID) REFERENCES unitgroup (ID);


-- update supportedlanguages property for existing datasets
INSERT INTO process_languages (process_id, supportedlanguages_id)
   SELECT p.process_id, l.id 
   FROM processname_base p, languages l WHERE l.LANGUAGECODE=p.lang;
