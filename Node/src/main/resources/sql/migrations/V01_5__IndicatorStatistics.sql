ALTER TABLE `clclass` ADD INDEX `clid` ( `CLID` );

ALTER TABLE `globalreference` ADD INDEX `uuid` ( `UUID` );

ALTER TABLE `lciaresult_amounts` ADD INDEX `module` ( `module` );

DROP TABLE IF EXISTS `process_lciaresult_clclass_statistics`;

CREATE TABLE `process_lciaresult_clclass_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `clid` varchar(255) CHARACTER SET utf8 NOT NULL,
  `unitgroup_uuid` varchar(255) CHARACTER SET utf8 NOT NULL,
  `module` varchar(255) CHARACTER SET utf8 NOT NULL,
  `val_min` float NOT NULL,
  `val_max` float NOT NULL,
  `val_mean` float NOT NULL,
  `ts_calculated` bigint(20) NOT NULL,
  `ts_last_change` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `clid` (`clid`),
  KEY `unitgroup_uuid` (`unitgroup_uuid`),
  KEY `module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;