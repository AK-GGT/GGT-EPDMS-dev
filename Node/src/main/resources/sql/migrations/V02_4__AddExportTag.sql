CREATE TABLE IF NOT EXISTS `datastock_export_tag` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` boolean DEFAULT NULL,
  `file` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

ALTER TABLE  `datastock` ADD `export_tag_id` bigint(20) DEFAULT NULL AFTER `owner_organization` ;

ALTER TABLE `datastock`
  ADD CONSTRAINT `FK_datastock_export_tag_id` FOREIGN KEY (`export_tag_id`) REFERENCES `datastock_export_tag` (`ID`);

