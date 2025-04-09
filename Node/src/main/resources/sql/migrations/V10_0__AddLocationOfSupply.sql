DROP TABLE IF EXISTS `flow_locationofsupply`;
CREATE TABLE IF NOT EXISTS `flow_locationofsupply` (
  `flow_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_flow_locationofsupply_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `flow_locationofsupply`
  ADD CONSTRAINT `FK_flow_locationofsupply_flow_id` FOREIGN KEY (`flow_id`) REFERENCES `flow_common` (`ID`);
