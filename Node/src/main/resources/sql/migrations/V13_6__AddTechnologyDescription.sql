DROP TABLE IF EXISTS `process_technologydescription`;
CREATE TABLE IF NOT EXISTS `process_technologydescription` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_process_technologydescription_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
