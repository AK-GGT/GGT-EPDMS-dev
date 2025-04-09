DROP TABLE IF EXISTS `glad_database_properties`;
CREATE TABLE IF NOT EXISTS `glad_database_properties` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `biogeniccarbonmodeling` varchar(255) DEFAULT NULL,
  `endoflifemodeling` varchar(255) DEFAULT NULL,
  `watermodeling` varchar(255) DEFAULT NULL,
  `infrastructuremodeling` varchar(255) DEFAULT NULL,
  `emissionmodeling` varchar(255) DEFAULT NULL,
  `carbonstoragemodeling` varchar(255) DEFAULT NULL,
  `representativenesstype` varchar(255) DEFAULT NULL,
  `reviewsystem` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;