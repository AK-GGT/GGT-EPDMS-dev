ALTER TABLE `user` ADD `sector_other` VARCHAR(255) DEFAULT NULL AFTER `JOBPOSITION`;

CREATE TABLE IF NOT EXISTS `user_sector` (
  `User_ID` bigint(20) NOT NULL,
  `sector` varchar(255) DEFAULT NULL,
  FOREIGN KEY (`User_ID`) REFERENCES `user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;