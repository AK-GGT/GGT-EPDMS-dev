ALTER TABLE `datastock_export_tag` ADD `mode` integer DEFAULT 2 AFTER `datastock_id`;

DELETE FROM `datastock_export_tag`;
