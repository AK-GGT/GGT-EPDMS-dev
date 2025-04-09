DROP TABLE IF EXISTS `flow_product_material_property`;

CREATE TABLE `flow_product_material_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `definition_id` bigint(20) DEFAULT NULL,
  `value` double DEFAULT NULL,
  `product_flow_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `definition_id` (`definition_id`),
  KEY `product_flow_id` (`product_flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_product_material_property_definition`;

CREATE TABLE `flow_product_material_property_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `unitDescription` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `flow_product_material_property`
  ADD CONSTRAINT `FK_flow_product_material_property_product` FOREIGN KEY (`product_flow_id`) REFERENCES `flow_product` (`ID`),
  ADD CONSTRAINT `FK_flow_product_material_property_definition` FOREIGN KEY (`definition_id`) REFERENCES `flow_product_material_property_definition` (`id`);