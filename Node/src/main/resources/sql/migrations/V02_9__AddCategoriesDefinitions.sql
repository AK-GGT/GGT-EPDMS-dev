CREATE TABLE categorydefinition (ID BIGINT AUTO_INCREMENT NOT NULL, IMPORTDATE DATETIME, MOSTRECENTVERSION TINYINT(1) default 0, XMLFILE_ID BIGINT, PRIMARY KEY (ID));
CREATE TABLE categorysystem_categorydefinition (CategorySystem_ID BIGINT NOT NULL, categoryDefinitions_ID BIGINT NOT NULL, PRIMARY KEY (CategorySystem_ID, categoryDefinitions_ID));

ALTER TABLE categorysystem_categorydefinition ADD CONSTRAINT categorysystem_categorydefinitionCategorySystem_ID FOREIGN KEY (CategorySystem_ID) REFERENCES categorysystem (ID);
ALTER TABLE categorysystem_categorydefinition ADD CONSTRAINT ctgorysystemcategorydefinitionctegoryDefinitionsID FOREIGN KEY (categoryDefinitions_ID) REFERENCES categorydefinition (ID);

ALTER TABLE categorydefinition ADD CONSTRAINT FK_categorydefinition_XMLFILE_ID FOREIGN KEY (XMLFILE_ID) REFERENCES xmlfile (ID);
