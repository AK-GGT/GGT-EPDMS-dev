DROP TABLE IF EXISTS `config2`;
CREATE TABLE IF NOT EXISTS `config2` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `property` varchar(255) NOT NULL,
  `stringvalue` varchar(255) DEFAULT NULL,
  `intvalue` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

INSERT INTO `config2` (`property`, `intvalue`) VALUES ('datastock.default', (SELECT `default_datastock_id` FROM `configuration`));
INSERT INTO `config2` (`property`, `intvalue`) VALUES ('datastock.default_isroot', (SELECT `default_datastock_is_root` FROM `configuration`));
INSERT INTO `config2` (`property`, `stringvalue`) VALUES ('classification.default_classification_system', (SELECT `default_classification_system` FROM `configuration`));

DROP TABLE `configuration`;

RENAME TABLE `config2` TO `configuration`;