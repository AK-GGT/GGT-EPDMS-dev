CREATE TABLE IF NOT EXISTS `user_acceptedaddterms` (
  `User_ID` bigint(20) NOT NULL,
  `additionalterm` varchar(255) DEFAULT NULL,
  `accepted` boolean DEFAULT NULL,
  FOREIGN KEY (`User_ID`) REFERENCES `user` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;