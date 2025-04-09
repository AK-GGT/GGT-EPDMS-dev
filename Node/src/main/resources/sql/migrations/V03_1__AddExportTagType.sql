ALTER TABLE `datastock_export_tag` ADD `datastock_id` bigint(20) AFTER `ID`;

UPDATE `datastock_export_tag` t SET datastock_id = ( SELECT s.ID from datastock s WHERE s.export_tag_id = t.id);

ALTER TABLE `datastock_export_tag` ADD CONSTRAINT `FK_datastock_export_tag_datastock_id` FOREIGN KEY (`datastock_id`) REFERENCES `datastock` (`ID`);

ALTER TABLE `datastock` DROP FOREIGN KEY `FK_datastock_export_tag_id`;
ALTER TABLE `datastock` DROP COLUMN `export_tag_id`;

ALTER TABLE `datastock_export_tag` ADD INDEX `datastockfk_idx` (`datastock_id` ASC);

ALTER TABLE `datastock_export_tag` ADD `type` integer DEFAULT 0 AFTER `datastock_id`;

UPDATE `datastock_export_tag` SET type = 0;
