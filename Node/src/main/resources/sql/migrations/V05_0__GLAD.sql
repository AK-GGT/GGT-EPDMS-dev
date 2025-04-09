DROP TABLE IF EXISTS `glad_registration_data`;
CREATE TABLE IF NOT EXISTS `glad_registration_data` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) DEFAULT NULL,
  `majorversion` int(11) DEFAULT NULL,
  `minorversion` int(11) DEFAULT NULL,
  `subminorversion` int(11) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;