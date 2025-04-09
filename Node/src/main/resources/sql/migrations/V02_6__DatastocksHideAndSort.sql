CREATE TABLE IF NOT EXISTS `datastock_display_props` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `hidden` boolean DEFAULT false,
  `ordinal` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

ALTER TABLE `datastock` ADD `display_props_id` bigint(20) DEFAULT NULL AFTER `owner_organization` ;

ALTER TABLE `datastock`
  ADD CONSTRAINT `FK_datastock_display_props_id` FOREIGN KEY (`display_props_id`) REFERENCES `datastock_display_props` (`ID`);

