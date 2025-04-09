/* This is a workaround to avoid having to change the lower_case_table_names system variable on the MySQL instance */

ALTER TABLE `contact_description` RENAME TO `Contact_DESCRIPTION_TMP`;
ALTER TABLE `Contact_DESCRIPTION_TMP` RENAME TO `Contact_DESCRIPTION`;

ALTER TABLE `contact_name` RENAME TO `Contact_NAME_TMP`;
ALTER TABLE `Contact_NAME_TMP` RENAME TO `Contact_NAME`;

ALTER TABLE `flowproperty_description` RENAME TO `FlowProperty_DESCRIPTION_TMP`;
ALTER TABLE `FlowProperty_DESCRIPTION_TMP` RENAME TO `FlowProperty_DESCRIPTION`;

ALTER TABLE `flowproperty_name` RENAME TO `FlowProperty_NAME_TMP`;
ALTER TABLE `FlowProperty_NAME_TMP` RENAME TO `FlowProperty_NAME`;

ALTER TABLE `flow_description` RENAME TO `Flow_DESCRIPTION_TMP`;
ALTER TABLE `Flow_DESCRIPTION_TMP` RENAME TO `Flow_DESCRIPTION`;

ALTER TABLE `flow_name` RENAME TO `Flow_NAME_TMP`;
ALTER TABLE `Flow_NAME_TMP` RENAME TO `Flow_NAME`;

ALTER TABLE `lciamethod_description` RENAME TO `LCIAMethod_DESCRIPTION_TMP`;
ALTER TABLE `LCIAMethod_DESCRIPTION_TMP` RENAME TO `LCIAMethod_DESCRIPTION`;

ALTER TABLE `lciamethod_name` RENAME TO `LCIAMethod_NAME_TMP`;
ALTER TABLE `LCIAMethod_NAME_TMP` RENAME TO `LCIAMethod_NAME`;

ALTER TABLE `process_description` RENAME TO `Process_DESCRIPTION_TMP`;
ALTER TABLE `Process_DESCRIPTION_TMP` RENAME TO `Process_DESCRIPTION`;

ALTER TABLE `process_name` RENAME TO `Process_NAME_TMP`;
ALTER TABLE `Process_NAME_TMP` RENAME TO `Process_NAME`;

ALTER TABLE `source_description` RENAME TO `Source_DESCRIPTION_TMP`;
ALTER TABLE `Source_DESCRIPTION_TMP` RENAME TO `Source_DESCRIPTION`;

ALTER TABLE `source_name` RENAME TO `Source_NAME_TMP`;
ALTER TABLE `Source_NAME_TMP` RENAME TO `Source_NAME`;

ALTER TABLE `source_shortname` RENAME TO `source_shortName_TMP`;
ALTER TABLE `source_shortName_TMP` RENAME TO `source_shortName`;

ALTER TABLE `unitgroup_description` RENAME TO `UnitGroup_DESCRIPTION_TMP`;
ALTER TABLE `UnitGroup_DESCRIPTION_TMP` RENAME TO `UnitGroup_DESCRIPTION`;

ALTER TABLE `unitgroup_name` RENAME TO `UnitGroup_NAME_TMP`;
ALTER TABLE `UnitGroup_NAME_TMP` RENAME TO `UnitGroup_NAME`;
