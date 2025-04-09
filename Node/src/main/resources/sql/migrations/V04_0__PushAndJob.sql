DROP TABLE IF EXISTS `pushtarget`;
CREATE TABLE IF NOT EXISTS `pushtarget` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `network_node_name` varchar(255) DEFAULT NULL,
  `network_node_id` varchar(255) DEFAULT NULL,
  `network_node_url` varchar(255) default NULL,
  `name` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `target_ds_name` varchar(255) DEFAULT NULL,
  `target_ds_uuid` varchar(255) DEFAULT NULL,
  `target_ds_descr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;




DROP TABLE IF EXISTS `pushconfig`;
CREATE TABLE IF NOT EXISTS `pushconfig` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `network_node_id` bigint(20) DEFAULT NULL,
  `source_datastock_id` bigint(20) DEFAULT NULL,
  `target_pushtarget_id` bigint(20) DEFAULT NULL,
  `dependencies_mode` tinyint DEFAULT 0,
  `name` varchar(255) DEFAULT NULL,
  `lastjobstate` tinyint DEFAULT 0,
  `lastpushdate` varchar(255) DEFAULT NULL,
  `favourite` boolean DEFAULT FALSE,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
  
ALTER TABLE `pushconfig` ADD CONSTRAINT `FK_pushconfig_datastock` FOREIGN KEY (`source_datastock_id`) REFERENCES `datastock` (`ID`);
ALTER TABLE `pushconfig` ADD CONSTRAINT `FK_pushconfig_pushtarget` FOREIGN KEY (`target_pushtarget_id`) REFERENCES `pushtarget` (`ID`);

DROP TABLE IF EXISTS `jobmetadata`;
CREATE TABLE IF NOT EXISTS `jobmetadata` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `jobid` varchar(255) DEFAULT NULL,
  `jobname` varchar(255) DEFAULT NULL,
  `jobtype` varchar(255) DEFAULT NULL,
  `firingtime` varchar(255) DEFAULT NULL,
  `completiontime` varchar(255) DEFAULT NULL,
  `runtime` bigint(20) DEFAULT NULL,
  `jobstate` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `log` mediumtext DEFAULT NULL,
  `errorscount` bigint(20) DEFAULT NULL,
  `infoscount` bigint(20) DEFAULT NULL,
  `successescount` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `jobid` (`jobid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

ALTER TABLE `jobmetadata` ADD CONSTRAINT `FK_jobmetadata_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`ID`);