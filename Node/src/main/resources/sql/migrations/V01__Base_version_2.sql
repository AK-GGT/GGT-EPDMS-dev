DROP TABLE IF EXISTS `category`;
CREATE TABLE IF NOT EXISTS `category` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CATID` varchar(255) DEFAULT NULL,
  `CATLEVEL` int(11) DEFAULT NULL,
  `CATNAME` varchar(255) DEFAULT NULL,
  `DATASETTYPE` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `categorysystem`;
CREATE TABLE IF NOT EXISTS `categorysystem` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `csname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `categorysystem_category`;
CREATE TABLE IF NOT EXISTS `categorysystem_category` (
  `CategorySystem_ID` bigint(20) NOT NULL,
  `categories_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`CategorySystem_ID`,`categories_ID`),
  KEY `FK_categorysystem_category_categories_ID` (`categories_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `classification`;
CREATE TABLE IF NOT EXISTS `classification` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `clname` varchar(255) DEFAULT NULL,
  `catSystem` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_classification_catSystem` (`catSystem`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `classification_clclass`;
CREATE TABLE IF NOT EXISTS `classification_clclass` (
  `Classification_ID` bigint(20) NOT NULL,
  `classes_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Classification_ID`,`classes_ID`),
  KEY `FK_classification_clclass_classes_ID` (`classes_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `clclass`;
CREATE TABLE IF NOT EXISTS `clclass` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CLID` varchar(255) DEFAULT NULL,
  `DATASETTYPE` varchar(255) DEFAULT NULL,
  `clLevel` int(11) DEFAULT NULL,
  `clName` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `CATEGORY_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_clclass_CATEGORY_ID` (`CATEGORY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `compliancesystem`;
CREATE TABLE IF NOT EXISTS `compliancesystem` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DOCUMENTATIONCOMPLIANCE` varchar(255) DEFAULT NULL,
  `METHODOLOGICALCOMPLIANCE` varchar(255) DEFAULT NULL,
  `NOMENCLATURECOMPLIANCE` varchar(255) DEFAULT NULL,
  `OVERALLCOMPLIANCE` varchar(255) DEFAULT NULL,
  `QUALITYCOMPLIANCE` varchar(255) DEFAULT NULL,
  `REVIEWCOMPLIANCE` varchar(255) DEFAULT NULL,
  `SOURCEREFERENCE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_compliancesystem_SOURCEREFERENCE_ID` (`SOURCEREFERENCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `configuration`;
CREATE TABLE IF NOT EXISTS `configuration` (
  `default_datastock_is_root` tinyint(1) DEFAULT NULL,
  `default_datastock_id` bigint(20) NOT NULL,
  `default_classification_system` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `configuration` (`default_datastock_is_root`, `default_datastock_id`, `default_classification_system`) VALUES(1, 1, 'ILCD');

DROP TABLE IF EXISTS `contact`;
CREATE TABLE IF NOT EXISTS `contact` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `CENTRALCONTACTPOINT` varchar(255) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `CONTACTADDRESS` varchar(255) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `FAX` varchar(255) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `PHONE` varchar(255) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `WWW` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_contact_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_contact_root_stock_id` (`root_stock_id`),
  KEY `FK_contact_XMLFILE_ID` (`XMLFILE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `contact_classifications`;
CREATE TABLE IF NOT EXISTS `contact_classifications` (
  `Contact_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Contact_ID`,`classifications_ID`),
  KEY `FK_contact_classifications_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `contact_description`;
CREATE TABLE IF NOT EXISTS `contact_description` (
  `contact_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Contact_DESCRIPTION_contact_id` (`contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `contact_name`;
CREATE TABLE IF NOT EXISTS `contact_name` (
  `contact_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Contact_NAME_contact_id` (`contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `contact_shortname`;
CREATE TABLE IF NOT EXISTS `contact_shortname` (
  `contact_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_contact_shortname_contact_id` (`contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `dataqualityindicator`;
CREATE TABLE IF NOT EXISTS `dataqualityindicator` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `indicatorName` varchar(255) DEFAULT NULL,
  `indicatorValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `dataset_deregistration_reason`;
CREATE TABLE IF NOT EXISTS `dataset_deregistration_reason` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `REASON` longblob,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `dataset_registration_data`;
CREATE TABLE IF NOT EXISTS `dataset_registration_data` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `STATUS` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `REASON_ID` bigint(20) DEFAULT NULL,
  `REGISTRY_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_dataset_registration_data_REGISTRY_ID` (`REGISTRY_ID`),
  KEY `FK_dataset_registration_data_REASON_ID` (`REASON_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `datastock`;
CREATE TABLE IF NOT EXISTS `datastock` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `datastock_type` varchar(3) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `owner_organization` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_datastock_0` (`name`),
  KEY `FK_datastock_owner_organization` (`owner_organization`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

INSERT INTO `datastock` (`ID`, `datastock_type`, `name`, `UUID`, `owner_organization`) VALUES(1, 'rds', 'default', '00000000-0000-0000-0000-000000000000', 1);

DROP TABLE IF EXISTS `datastock_contact`;
CREATE TABLE IF NOT EXISTS `datastock_contact` (
  `contacts_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`contacts_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_contact_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_description`;
CREATE TABLE IF NOT EXISTS `datastock_description` (
  `datastock_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_datastock_description_datastock_id` (`datastock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `datastock_description` (`datastock_id`, `value`, `lang`) VALUES(1, 'This is the default root data stock', 'en');

DROP TABLE IF EXISTS `datastock_flowproperty`;
CREATE TABLE IF NOT EXISTS `datastock_flowproperty` (
  `flowProperties_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`flowProperties_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_flowproperty_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_flow_elementary`;
CREATE TABLE IF NOT EXISTS `datastock_flow_elementary` (
  `elementaryFlows_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`elementaryFlows_ID`,`containingDataStocks_ID`),
  KEY `datastock_flow_elementary_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_flow_product`;
CREATE TABLE IF NOT EXISTS `datastock_flow_product` (
  `productFlows_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`productFlows_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_flow_product_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_lciamethod`;
CREATE TABLE IF NOT EXISTS `datastock_lciamethod` (
  `lciaMethods_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`lciaMethods_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_lciamethod_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_longtitle`;
CREATE TABLE IF NOT EXISTS `datastock_longtitle` (
  `datastock_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_datastock_longtitle_datastock_id` (`datastock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `datastock_longtitle` (`datastock_id`, `value`, `lang`) VALUES(1, 'Default root data stock', 'en');

DROP TABLE IF EXISTS `datastock_process`;
CREATE TABLE IF NOT EXISTS `datastock_process` (
  `processes_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`processes_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_process_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_source`;
CREATE TABLE IF NOT EXISTS `datastock_source` (
  `sources_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`sources_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_source_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `datastock_unitgroup`;
CREATE TABLE IF NOT EXISTS `datastock_unitgroup` (
  `unitGroups_ID` bigint(20) NOT NULL,
  `containingDataStocks_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`unitGroups_ID`,`containingDataStocks_ID`),
  KEY `FK_datastock_unitgroup_containingDataStocks_ID` (`containingDataStocks_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `digitalfile`;
CREATE TABLE IF NOT EXISTS `digitalfile` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `FILENAME` varchar(1000) DEFAULT NULL,
  `SOURCE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_digitalfile_SOURCE_ID` (`SOURCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `exchange`;
CREATE TABLE IF NOT EXISTS `exchange` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALLOCATION` varchar(255) DEFAULT NULL,
  `DATASOURCE` varchar(255) DEFAULT NULL,
  `DERIVATIONTYPE` varchar(255) DEFAULT NULL,
  `EXCHANGEDIRECTION` varchar(255) DEFAULT NULL,
  `FUNCTIONTYPE` varchar(255) DEFAULT NULL,
  `INTERNALID` int(11) DEFAULT NULL,
  `LOCATION` varchar(255) DEFAULT NULL,
  `MAXIMUMAMOUNT` float DEFAULT NULL,
  `MEANAMOUNT` float DEFAULT NULL,
  `MINIMUMAMOUNT` float DEFAULT NULL,
  `REFERENCETOVARIABLE` varchar(255) DEFAULT NULL,
  `RESULTINGAMOUNT` float DEFAULT NULL,
  `STANDARDDEVIATION` float DEFAULT NULL,
  `UNCERTAINTYDISTRIBUTION` varchar(255) DEFAULT NULL,
  `FLOW_ID` bigint(20) DEFAULT NULL,
  `FLOWREFERENCE_ID` bigint(20) DEFAULT NULL,
  `REFTODATASOURCE_ID` bigint(20) DEFAULT NULL,
  `unitgroup_reference` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_exchange_FLOWREFERENCE_ID` (`FLOWREFERENCE_ID`),
  KEY `FK_exchange_FLOW_ID` (`FLOW_ID`),
  KEY `FK_exchange_REFTODATASOURCE_ID` (`REFTODATASOURCE_ID`),
  KEY `FK_exchange_unitgroup_reference` (`unitgroup_reference`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `exchange_amounts`;
CREATE TABLE IF NOT EXISTS `exchange_amounts` (
  `phase` varchar(255) DEFAULT NULL,
  `module` varchar(255) DEFAULT NULL,
  `scenario` varchar(255) DEFAULT NULL,
  `value` float DEFAULT NULL,
  `exchange_id` bigint(20) DEFAULT NULL,
  KEY `FK_Exchange_AMOUNTS_exchange_id` (`exchange_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `exchange_comment`;
CREATE TABLE IF NOT EXISTS `exchange_comment` (
  `exchange_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_exchange_comment_exchange_id` (`exchange_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flowproperty`;
CREATE TABLE IF NOT EXISTS `flowproperty` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `defaultUnit_cache` varchar(10) DEFAULT NULL,
  `defaultUnitGroup_cache` varchar(20) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `REFERENCETOUNITGROUP_ID` bigint(20) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `UNITGROUP_ID` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_flowproperty_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_flowproperty_REFERENCETOUNITGROUP_ID` (`REFERENCETOUNITGROUP_ID`),
  KEY `FK_flowproperty_root_stock_id` (`root_stock_id`),
  KEY `FK_flowproperty_XMLFILE_ID` (`XMLFILE_ID`),
  KEY `FK_flowproperty_UNITGROUP_ID` (`UNITGROUP_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `flowpropertydescription`;
CREATE TABLE IF NOT EXISTS `flowpropertydescription` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DERIVATIONTYPE` varchar(255) DEFAULT NULL,
  `INTERNALID` int(11) DEFAULT NULL,
  `maximumValue` double DEFAULT NULL,
  `MEANVALUE` double DEFAULT NULL,
  `MINVALUE` double DEFAULT NULL,
  `STANDARDDEVIATION` float DEFAULT NULL,
  `UNCERTAINTYTYPE` varchar(255) DEFAULT NULL,
  `FLOWPROPERTY_ID` bigint(20) DEFAULT NULL,
  `FLOWPROPERTYREF_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_flowpropertydescription_FLOWPROPERTYREF_ID` (`FLOWPROPERTYREF_ID`),
  KEY `FK_flowpropertydescription_FLOWPROPERTY_ID` (`FLOWPROPERTY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `flowpropertydescription_description`;
CREATE TABLE IF NOT EXISTS `flowpropertydescription_description` (
  `flowpropertydescription_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `flwprprtydscrptiondescriptionflwprprtydscriptionid` (`flowpropertydescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flowproperty_classifications`;
CREATE TABLE IF NOT EXISTS `flowproperty_classifications` (
  `FlowProperty_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`FlowProperty_ID`,`classifications_ID`),
  KEY `FK_flowproperty_classifications_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flowproperty_description`;
CREATE TABLE IF NOT EXISTS `flowproperty_description` (
  `flowproperty_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_FlowProperty_DESCRIPTION_flowproperty_id` (`flowproperty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flowproperty_name`;
CREATE TABLE IF NOT EXISTS `flowproperty_name` (
  `flowproperty_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_FlowProperty_NAME_flowproperty_id` (`flowproperty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flowproperty_synonyms`;
CREATE TABLE IF NOT EXISTS `flowproperty_synonyms` (
  `flowproperty_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_flowproperty_synonyms_flowproperty_id` (`flowproperty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_common`;
CREATE TABLE IF NOT EXISTS `flow_common` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `flow_object_type` varchar(31) DEFAULT NULL,
  `BRANCH` int(11) DEFAULT NULL,
  `CASNUMBER` varchar(255) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `referenceProperty_cache` varchar(20) DEFAULT NULL,
  `referencePropertyUnit_cache` varchar(10) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `SUMFORMULA` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `reference_property_description_id` bigint(20) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_flow_common_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_flow_common_root_stock_id` (`root_stock_id`),
  KEY `FK_flow_common_XMLFILE_ID` (`XMLFILE_ID`),
  KEY `FK_flow_common_reference_property_description_id` (`reference_property_description_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `flow_common_classification`;
CREATE TABLE IF NOT EXISTS `flow_common_classification` (
  `Flow_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Flow_ID`,`classifications_ID`),
  KEY `FK_flow_common_classification_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_description`;
CREATE TABLE IF NOT EXISTS `flow_description` (
  `Flow_ID` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Flow_DESCRIPTION_Flow_ID` (`Flow_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_elementary`;
CREATE TABLE IF NOT EXISTS `flow_elementary` (
  `ID` bigint(20) NOT NULL,
  `categorization` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_flow_elementary_categorization` (`categorization`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_name`;
CREATE TABLE IF NOT EXISTS `flow_name` (
  `Flow_ID` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Flow_NAME_Flow_ID` (`Flow_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_product`;
CREATE TABLE IF NOT EXISTS `flow_product` (
  `ID` bigint(20) NOT NULL,
  `specific_product` tinyint(1) DEFAULT '0',
  `type` varchar(255) DEFAULT NULL,
  `is_a_reference` bigint(20) DEFAULT NULL,
  `source_reference` bigint(20) DEFAULT NULL,
  `vendor_reference` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_flow_product_source_reference` (`source_reference`),
  KEY `FK_flow_product_vendor_reference` (`vendor_reference`),
  KEY `FK_flow_product_is_a_reference` (`is_a_reference`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_propertydescriptions`;
CREATE TABLE IF NOT EXISTS `flow_propertydescriptions` (
  `Flow_ID` bigint(20) NOT NULL,
  `propertyDescriptions_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Flow_ID`,`propertyDescriptions_ID`),
  KEY `flow_propertydescriptions_propertyDescriptions_ID` (`propertyDescriptions_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `flow_synonyms`;
CREATE TABLE IF NOT EXISTS `flow_synonyms` (
  `flow_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_flow_synonyms_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `geographicalarea`;
CREATE TABLE IF NOT EXISTS `geographicalarea` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `AREACODE` varchar(255) DEFAULT NULL,
  `areaName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=270 ;

INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(1, 'GLO', 'Global');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(2, 'OCE', 'Oceanic');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(3, 'RAF', 'Africa');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(4, 'RAS', 'Asia and the Pacific');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(5, 'RER', 'Europe');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(6, 'RLA', 'Latin America & the Caribbean');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(7, 'RNA', 'North America');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(8, 'RNE', 'Near East');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(9, 'RME', 'Middle East');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(10, 'EU-15', 'EU 15');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(11, 'EU-NMC', 'EU new member countries 2004');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(12, 'EU-25', 'EU 25');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(13, 'EC-CC', 'EU candidate countries 2005');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(14, 'EU-25&CC', 'EU 25 plus candidate countries 2005');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(15, 'EU-AC', 'EU associated countries 2005');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(16, 'EU-25&CC&AC', 'EU 25 plus candidate countries 2005 plus associated countries 2005');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(17, 'EU-27', 'EU-27 Member States');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(18, 'WEU', 'Western Europe');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(19, 'PAO', 'Pacific OECD (Japan, Australia, New Zealand)');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(20, 'FSU', 'Independent States of the Former Soviet Union');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(21, 'EEU', 'Central and Eastern Europe');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(22, 'MEA', 'Middle East and North Africa');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(23, 'AFR', 'Sub-Sahara Africa');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(24, 'CPA', 'Centrally Planned Asia and China');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(25, 'PAS', 'Other Pacific Asia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(26, 'SAS', 'South Asia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(27, 'UCTE', 'Union for the Co-ordination of Transmission of Electricity ');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(28, 'CENTREL', 'Central european power association');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(29, 'NORDEL', 'Nordic countries power association');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(30, 'AF', 'Afghanistan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(31, 'AX', 'Åland Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(32, 'AL', 'Albania');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(33, 'DZ', 'Algeria');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(34, 'AS', 'American Samoa');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(35, 'AD', 'Andorra');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(36, 'AO', 'Angola');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(37, 'AI', 'Anguilla');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(38, 'AQ', 'Antarctica');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(39, 'AG', 'Antigua and Barbuda');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(40, 'AR', 'Argentina');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(41, 'AM', 'Armenia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(42, 'AW', 'Aruba');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(43, 'AU', 'Australia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(44, 'AT', 'Austria');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(45, 'AZ', 'Azerbaijan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(46, 'BS', 'Bahamas');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(47, 'BH', 'Bahrain');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(48, 'BD', 'Bangladesh');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(49, 'BB', 'Barbados');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(50, 'BY', 'Belarus');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(51, 'BE', 'Belgium');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(52, 'BZ', 'Belize');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(53, 'BJ', 'Benin');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(54, 'BM', 'Bermuda');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(55, 'BT', 'Bhutan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(56, 'BO', 'Bolivia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(57, 'BA', 'Bosnia and Herzegovina');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(58, 'BW', 'Botswana');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(59, 'BV', 'Bouvet Island');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(60, 'BR', 'Brazil');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(61, 'IO', 'British Indian Ocean Territory');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(62, 'BN', 'Brunei Darussalam');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(63, 'BG', 'Bulgaria');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(64, 'BF', 'Burkina Faso');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(65, 'BI', 'Burundi');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(66, 'KH', 'Cambodia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(67, 'CM', 'Cameroon');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(68, 'CA', 'Canada');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(69, 'CV', 'Cape Verde');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(70, 'KY', 'Cayman Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(71, 'CF', 'Central African Republic');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(72, 'TD', 'Chad');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(73, 'CL', 'Chile');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(74, 'CN', 'China');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(75, 'CX', 'Christmas Island');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(76, 'CC', 'Cocos (Keeling) Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(77, 'CO', 'Colombia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(78, 'KM', 'Comoros');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(79, 'CG', 'Congo');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(80, 'CD', 'Congo, The Democratic Republic Of The');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(81, 'CK', 'Cook Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(82, 'CR', 'Costa Rica');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(83, 'CI', 'Cote D''ivoire');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(84, 'HR', 'Croatia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(85, 'CU', 'Cuba');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(86, 'CY', 'Cyprus');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(87, 'CZ', 'Czech Republic');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(88, 'DK', 'Denmark');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(89, 'DJ', 'Djibouti');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(90, 'DM', 'Dominica');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(91, 'DO', 'Dominican Republic');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(92, 'EC', 'Ecuador');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(93, 'EG', 'Egypt');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(94, 'SV', 'El Salvador');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(95, 'GQ', 'Equatorial Guinea');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(96, 'ER', 'Eritrea');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(97, 'EE', 'Estonia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(98, 'ET', 'Ethiopia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(99, 'FK', 'Falkland Islands (Malvinas)');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(100, 'FO', 'Faroe Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(101, 'FJ', 'Fiji');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(102, 'FI', 'Finland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(103, 'FR', 'France');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(104, 'GF', 'French Guiana');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(105, 'PF', 'French Polynesia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(106, 'TF', 'French Southern Territories');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(107, 'GA', 'Gabon');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(108, 'GM', 'Gambia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(109, 'GE', 'Georgia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(110, 'DE', 'Germany');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(111, 'GH', 'Ghana');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(112, 'GI', 'Gibraltar');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(113, 'GR', 'Greece');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(114, 'GL', 'Greenland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(115, 'GD', 'Grenada');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(116, 'GP', 'Guadeloupe');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(117, 'GU', 'Guam');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(118, 'GT', 'Guatemala');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(119, 'GN', 'Guinea');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(120, 'GW', 'Guinea-bissau');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(121, 'GY', 'Guyana');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(122, 'HT', 'Haiti');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(123, 'HM', 'Heard Island and Mcdonald Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(124, 'VA', 'Holy See (Vatican City State)');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(125, 'HN', 'Honduras');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(126, 'HK', 'Hong Kong');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(127, 'HU', 'Hungary');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(128, 'IS', 'Iceland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(129, 'IN', 'India');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(130, 'ID', 'Indonesia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(131, 'IR', 'Iran, Islamic Republic Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(132, 'IQ', 'Iraq');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(133, 'IE', 'Ireland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(134, 'IL', 'Israel');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(135, 'IT', 'Italy');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(136, 'JM', 'Jamaica');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(137, 'JP', 'Japan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(138, 'JO', 'Jordan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(139, 'KZ', 'Kazakhstan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(140, 'KE', 'Kenya');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(141, 'KI', 'Kiribati');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(142, 'KP', 'Korea, Democratic People''s Republic Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(143, 'KR', 'Korea, Republic Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(144, 'KW', 'Kuwait');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(145, 'KG', 'Kyrgyzstan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(146, 'LA', 'Lao People''s Democratic Republic');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(147, 'LV', 'Latvia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(148, 'LB', 'Lebanon');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(149, 'LS', 'Lesotho');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(150, 'LR', 'Liberia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(151, 'LY', 'Libyan Arab Jamahiriya');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(152, 'LI', 'Liechtenstein');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(153, 'LT', 'Lithuania');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(154, 'LU', 'Luxembourg');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(155, 'MO', 'Macao');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(156, 'MK', 'Macedonia, The Former Yugoslav Republic Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(157, 'MG', 'Madagascar');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(158, 'MW', 'Malawi');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(159, 'MY', 'Malaysia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(160, 'MV', 'Maldives');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(161, 'ML', 'Mali');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(162, 'MT', 'Malta');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(163, 'MH', 'Marshall Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(164, 'MQ', 'Martinique');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(165, 'MR', 'Mauritania');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(166, 'MU', 'Mauritius');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(167, 'YT', 'Mayotte');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(168, 'MX', 'Mexico');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(169, 'FM', 'Micronesia, Federated States Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(170, 'MD', 'Moldova, Republic Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(171, 'MC', 'Monaco');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(172, 'MN', 'Mongolia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(173, 'MS', 'Montserrat');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(174, 'MA', 'Morocco');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(175, 'MZ', 'Mozambique');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(176, 'MM', 'Myanmar');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(177, 'NA', 'Namibia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(178, 'NR', 'Nauru');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(179, 'NP', 'Nepal');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(180, 'NL', 'Netherlands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(181, 'AN', 'Netherlands Antilles');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(182, 'NC', 'New Caledonia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(183, 'NZ', 'New Zealand');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(184, 'NI', 'Nicaragua');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(185, 'NE', 'Niger');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(186, 'NG', 'Nigeria');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(187, 'NU', 'Niue');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(188, 'NF', 'Norfolk Island');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(189, 'MP', 'Northern Mariana Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(190, 'NO', 'Norway');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(191, 'OM', 'Oman');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(192, 'PK', 'Pakistan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(193, 'PW', 'Palau');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(194, 'PS', 'Palestinian Territory, Occupied');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(195, 'PA', 'Panama');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(196, 'PG', 'Papua New Guinea');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(197, 'PY', 'Paraguay');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(198, 'PE', 'Peru');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(199, 'PH', 'Philippines');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(200, 'PN', 'Pitcairn');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(201, 'PL', 'Poland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(202, 'PT', 'Portugal');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(203, 'PR', 'Puerto Rico');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(204, 'QA', 'Qatar');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(205, 'RE', 'Reunion');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(206, 'RO', 'Romania');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(207, 'RU', 'Russian Federation');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(208, 'RW', 'Rwanda');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(209, 'SH', 'Saint Helena');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(210, 'KN', 'Saint Kitts and Nevis');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(211, 'LC', 'Saint Lucia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(212, 'PM', 'Saint Pierre and Miquelon');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(213, 'VC', 'Saint Vincent and The Grenadines');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(214, 'WS', 'Samoa');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(215, 'SM', 'San Marino');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(216, 'ST', 'Sao Tome and Principe');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(217, 'SA', 'Saudi Arabia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(218, 'SN', 'Senegal');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(219, 'CS', 'Serbia and Montenegro');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(220, 'SC', 'Seychelles');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(221, 'SL', 'Sierra Leone');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(222, 'SG', 'Singapore');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(223, 'SK', 'Slovakia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(224, 'SI', 'Slovenia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(225, 'SB', 'Solomon Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(226, 'SO', 'Somalia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(227, 'ZA', 'South Africa');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(228, 'GS', 'South Georgia and The South Sandwich Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(229, 'ES', 'Spain');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(230, 'LK', 'Sri Lanka');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(231, 'SD', 'Sudan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(232, 'SR', 'Suriname');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(233, 'SJ', 'Svalbard and Jan Mayen');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(234, 'SZ', 'Swaziland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(235, 'SE', 'Sweden');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(236, 'CH', 'Switzerland');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(237, 'SY', 'Syrian Arab Republic');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(238, 'TW', 'Taiwan, Province Of China');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(239, 'TJ', 'Tajikistan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(240, 'TZ', 'Tanzania, United Republic Of');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(241, 'TH', 'Thailand');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(242, 'TL', 'Timor-leste');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(243, 'TG', 'Togo');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(244, 'TK', 'Tokelau');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(245, 'TO', 'Tonga');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(246, 'TT', 'Trinidad and Tobago');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(247, 'TN', 'Tunisia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(248, 'TR', 'Turkey');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(249, 'TM', 'Turkmenistan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(250, 'TC', 'Turks and Caicos Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(251, 'TV', 'Tuvalu');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(252, 'UG', 'Uganda');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(253, 'UA', 'Ukraine');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(254, 'AE', 'United Arab Emirates');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(255, 'GB', 'United Kingdom');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(256, 'US', 'United States');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(257, 'UM', 'United States Minor Outlying Islands');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(258, 'UY', 'Uruguay');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(259, 'UZ', 'Uzbekistan');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(260, 'VU', 'Vanuatu');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(261, 'VE', 'Venezuela');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(262, 'VN', 'Vietnam');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(263, 'VG', 'Virgin Islands, British');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(264, 'VI', 'Virgin Islands, U.S.');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(265, 'WF', 'Wallis and Futuna');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(266, 'EH', 'Western Sahara');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(267, 'YE', 'Yemen');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(268, 'ZM', 'Zambia');
INSERT INTO `geographicalarea` (`ID`, `AREACODE`, `areaName`) VALUES(269, 'ZW', 'Zimbabwe');

DROP TABLE IF EXISTS `globalreference`;
CREATE TABLE IF NOT EXISTS `globalreference` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE` varchar(255) DEFAULT NULL,
  `URI` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `globalreference_shortdescription`;
CREATE TABLE IF NOT EXISTS `globalreference_shortdescription` (
  `globalreference_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `globalreference_shortdescriptionglobalreference_id` (`globalreference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `globalreference_subreferences`;
CREATE TABLE IF NOT EXISTS `globalreference_subreferences` (
  `globalreference_id` bigint(20) DEFAULT NULL,
  `SUBREFERENCES` varchar(255) DEFAULT NULL,
  KEY `globalreference_subreferences_globalreference_id` (`globalreference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `industrialsector`;
CREATE TABLE IF NOT EXISTS `industrialsector` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `SECTOR` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `SECTOR` (`SECTOR`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=10 ;

INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(1, 'Agro-industry');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(4, 'Chemical (Industrial & Agro)');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(3, 'Electrical and Electronic');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(7, 'General Consumer Goods');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(5, 'Heavy Industry (Cemont, Iron, Steel, Aluminium)');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(9, 'Others');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(2, 'Petrolium, Petrochemical and Plastic');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(6, 'Utilities & Services (Electricity, Water supply)');
INSERT INTO `industrialsector` (`ID`, `SECTOR`) VALUES(8, 'Waste Management');

DROP TABLE IF EXISTS `languages`;
CREATE TABLE IF NOT EXISTS `languages` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `LANGUAGECODE` varchar(255) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=140 ;

INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(1, 'aa', 'Afar');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(2, 'ab', 'Abkhazian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(3, 'af', 'Afrikaans');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(4, 'am', 'Amharic');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(5, 'ar', 'Arabic');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(6, 'as', 'Assamese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(7, 'ay', 'Aymara');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(8, 'az', 'Azerbaijani');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(9, 'ba', 'Bashkir');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(10, 'be', 'Byelorussian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(11, 'bg', 'Bulgarian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(12, 'bh', 'Bihari');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(13, 'bi', 'Bislama');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(14, 'bn', 'Bengali');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(15, 'bo', 'Tibetan');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(16, 'br', 'Breton');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(17, 'ca', 'Catalan');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(18, 'co', 'Corsican');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(19, 'cs', 'Czech');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(20, 'cy', 'Welsh');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(21, 'da', 'Danish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(22, 'de', 'German');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(23, 'dz', 'Bhutani');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(24, 'el', 'Greek');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(25, 'en', 'English');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(26, 'eo', 'Esperanto');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(27, 'es', 'Spanish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(28, 'et', 'Estonian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(29, 'eu', 'Basque');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(30, 'fa', 'Persian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(31, 'fi', 'Finnish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(32, 'fj', 'Fiji');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(33, 'fo', 'Faroese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(34, 'fr', 'French');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(35, 'fy', 'Frisian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(36, 'ga', 'Irish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(37, 'gd', 'Scots Gaelic');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(38, 'gl', 'Galician');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(39, 'gn', 'Guarani');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(40, 'gu', 'Gujarati');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(41, 'ha', 'Hausa');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(42, 'he', 'Hebrew');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(43, 'hi', 'Hindi');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(44, 'hr', 'Croatian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(45, 'hu', 'Hungarian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(46, 'hy', 'Armenian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(47, 'ia', 'Interlingua');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(48, 'id', 'Indonesian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(49, 'ie', 'Interlingue');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(50, 'ik', 'Inupiak');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(51, 'is', 'Icelandic');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(52, 'it', 'Italian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(53, 'iu', 'Inuktitut');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(54, 'ja', 'Japanese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(55, 'jw', 'Javanese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(56, 'ka', 'Georgian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(57, 'kk', 'Kazakh');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(58, 'kl', 'Greenlandic');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(59, 'km', 'Cambodian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(60, 'kn', 'Kannada');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(61, 'ko', 'Korean');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(62, 'ks', 'Kashmiri');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(63, 'ku', 'Kurdish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(64, 'ky', 'Kirghiz');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(65, 'la', 'Latin');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(66, 'ln', 'Lingala');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(67, 'lo', 'Laothian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(68, 'lt', 'Lithuanian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(69, 'lv', 'Lettish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(70, 'mg', 'Malagasy');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(71, 'mi', 'Maori');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(72, 'mk', 'Macedonian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(73, 'ml', 'Malayalam');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(74, 'mn', 'Mongolian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(75, 'mo', 'Moldavian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(76, 'mr', 'Marathi');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(77, 'ms', 'Malay');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(78, 'mt', 'Maltese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(79, 'my', 'Burmese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(80, 'na', 'Nauru');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(81, 'ne', 'Nepali');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(82, 'nl', 'Dutch');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(83, 'no', 'Norwegian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(84, 'oc', 'Occitan');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(85, 'om', 'Oromo');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(86, 'or', 'Oriya');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(87, 'pa', 'Punjabi');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(88, 'pl', 'Polish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(89, 'ps', 'Pashto');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(90, 'pt', 'Portuguese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(91, 'qu', 'Quechua');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(92, 'rm', 'Rhaeto-Romance');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(93, 'rn', 'Kirundi');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(94, 'ro', 'Romanian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(95, 'ru', 'Russian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(96, 'rw', 'Kinyarwanda');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(97, 'sa', 'Sanskrit');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(98, 'sd', 'Sindhi');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(99, 'sg', 'Sangho');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(100, 'sh', 'Serbo-Croatian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(101, 'si', 'Sinhalese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(102, 'sk', 'Slovak');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(103, 'sl', 'Slovenian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(104, 'sm', 'Samoan');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(105, 'sn', 'Shona');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(106, 'so', 'Somali');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(107, 'sq', 'Albanian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(108, 'sr', 'Serbian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(109, 'ss', 'Siswati');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(110, 'st', 'Sesotho');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(111, 'su', 'Sundanese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(112, 'sv', 'Swedish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(113, 'sw', 'Swahili');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(114, 'ta', 'Tamil');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(115, 'te', 'Telugu');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(116, 'tg', 'Tajik');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(117, 'th', 'Thai');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(118, 'ti', 'Tigrinya');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(119, 'tk', 'Turkmen');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(120, 'tl', 'Tagalog');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(121, 'tn', 'Setswana');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(122, 'to', 'Tonga');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(123, 'tr', 'Turkish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(124, 'ts', 'Tsonga');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(125, 'tt', 'Tatar');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(126, 'tw', 'Twi');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(127, 'ug', 'Uighur');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(128, 'uk', 'Ukrainian');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(129, 'ur', 'Urdu');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(130, 'uz', 'Uzbek');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(131, 'vi', 'Vietnamese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(132, 'vo', 'Volapuk');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(133, 'wo', 'Wolof');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(134, 'xh', 'Xhosa');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(135, 'yi', 'Yiddish');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(136, 'yo', 'Yoruba');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(137, 'za', 'Zhuang');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(138, 'zh', 'Chinese');
INSERT INTO `languages` (`ID`, `LANGUAGECODE`, `NAME`) VALUES(139, 'zu', 'Zulu');

DROP TABLE IF EXISTS `lciamethod`;
CREATE TABLE IF NOT EXISTS `lciamethod` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `IMPACTINDICATOR` varchar(500) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_lciamethod_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_lciamethod_root_stock_id` (`root_stock_id`),
  KEY `FK_lciamethod_XMLFILE_ID` (`XMLFILE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `lciamethodcharacterisationfactor`;
CREATE TABLE IF NOT EXISTS `lciamethodcharacterisationfactor` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DATADERIVATIONTYPESTATUS` varchar(255) DEFAULT NULL,
  `EXCHANGEDIRECTION` varchar(255) DEFAULT NULL,
  `MEANVALUE` float DEFAULT NULL,
  `FLOWGLOBALREFERENCE_ID` bigint(20) DEFAULT NULL,
  `REFERENCEDFLOWINSTANCE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `lcmethodcharacterisationfactorFLWGLOBALREFERENCEID` (`FLOWGLOBALREFERENCE_ID`),
  KEY `lcmthodcharacterisationfactorRFRNCEDFLOWINSTANCEID` (`REFERENCEDFLOWINSTANCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `lciamethodcharfactor_description`;
CREATE TABLE IF NOT EXISTS `lciamethodcharfactor_description` (
  `lciamethodcharacterisationfactor_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lcmthdchrfctrdescriptionlcmthdchrctrsationfactorid` (`lciamethodcharacterisationfactor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_areaofprotection`;
CREATE TABLE IF NOT EXISTS `lciamethod_areaofprotection` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `AREAOFPROTECTION` varchar(255) DEFAULT NULL,
  KEY `FK_lciamethod_areaofprotection_lciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_classifications`;
CREATE TABLE IF NOT EXISTS `lciamethod_classifications` (
  `LCIAMethod_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`LCIAMethod_ID`,`classifications_ID`),
  KEY `FK_lciamethod_classifications_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_description`;
CREATE TABLE IF NOT EXISTS `lciamethod_description` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_LCIAMethod_DESCRIPTION_lciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_impactcategory`;
CREATE TABLE IF NOT EXISTS `lciamethod_impactcategory` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `IMPACTCATEGORY` varchar(255) DEFAULT NULL,
  KEY `FK_lciamethod_impactcategory_lciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_lciamethodcharacterisationfactor`;
CREATE TABLE IF NOT EXISTS `lciamethod_lciamethodcharacterisationfactor` (
  `LCIAMethod_ID` bigint(20) NOT NULL,
  `characterisationFactors_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`LCIAMethod_ID`,`characterisationFactors_ID`),
  KEY `lcmthdlcmthdcharacterisationfactorchrctrstnFctrsID` (`characterisationFactors_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_methodology`;
CREATE TABLE IF NOT EXISTS `lciamethod_methodology` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `METHODOLOGY` varchar(255) DEFAULT NULL,
  KEY `FK_lciamethod_methodology_lciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_name`;
CREATE TABLE IF NOT EXISTS `lciamethod_name` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_LCIAMethod_NAME_lciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_ti_durationdescription`;
CREATE TABLE IF NOT EXISTS `lciamethod_ti_durationdescription` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_lciamethod_ti_durationdescription_lciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciamethod_ti_referenceyeardescription`;
CREATE TABLE IF NOT EXISTS `lciamethod_ti_referenceyeardescription` (
  `lciamethod_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lciamethodti_referenceyeardescriptionlciamethod_id` (`lciamethod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciaresult`;
CREATE TABLE IF NOT EXISTS `lciaresult` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `MEANAMOUNT` float DEFAULT NULL,
  `STANDARDDEVIATION` float DEFAULT NULL,
  `UNCERTAINTYDISTRIBUTION` varchar(255) DEFAULT NULL,
  `METHODREFERENCE_ID` bigint(20) DEFAULT NULL,
  `unitgroup_reference` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_lciaresult_unitgroup_reference` (`unitgroup_reference`),
  KEY `FK_lciaresult_METHODREFERENCE_ID` (`METHODREFERENCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `lciaresult_amounts`;
CREATE TABLE IF NOT EXISTS `lciaresult_amounts` (
  `phase` varchar(255) DEFAULT NULL,
  `module` varchar(255) DEFAULT NULL,
  `scenario` varchar(255) DEFAULT NULL,
  `value` float DEFAULT NULL,
  `lciaresult_id` bigint(20) DEFAULT NULL,
  KEY `FK_LciaResult_AMOUNTS_lciaresult_id` (`lciaresult_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lciaresult_comment`;
CREATE TABLE IF NOT EXISTS `lciaresult_comment` (
  `lciaresult_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_lciaresult_comment_lciaresult_id` (`lciaresult_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lcimethodinformation`;
CREATE TABLE IF NOT EXISTS `lcimethodinformation` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `METHODPRINCIPLE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `networknode`;
CREATE TABLE IF NOT EXISTS `networknode` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ADMINEMAILADDRESS` varchar(255) DEFAULT NULL,
  `ADMINNAME` varchar(255) DEFAULT NULL,
  `ADMINPHONE` varchar(255) DEFAULT NULL,
  `ADMINWWWADDRESS` varchar(255) DEFAULT NULL,
  `BASEURL` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `NAME` varchar(255) NOT NULL,
  `NODEID` varchar(255) NOT NULL,
  `OPERATOR` varchar(255) DEFAULT NULL,
  `REGISTRY_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `BASEURL` (`BASEURL`),
  UNIQUE KEY `NAME` (`NAME`),
  UNIQUE KEY `NODEID` (`NODEID`),
  KEY `FK_networknode_REGISTRY_ID` (`REGISTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `organization`;
CREATE TABLE IF NOT EXISTS `organization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ORGANISATIONUNIT` varchar(255) DEFAULT NULL,
  `CITY` varchar(255) DEFAULT NULL,
  `COUNTRY` varchar(255) DEFAULT NULL,
  `STREETADDRESS` varchar(255) DEFAULT NULL,
  `ZIPCODE` varchar(255) DEFAULT NULL,
  `admin_group` bigint(20) DEFAULT NULL,
  `admin_user` bigint(20) DEFAULT NULL,
  `SECTOR_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_organization_admin_user` (`admin_user`),
  KEY `FK_organization_SECTOR_ID` (`SECTOR_ID`),
  KEY `FK_organization_admin_group` (`admin_group`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

INSERT INTO `organization` (`id`, `name`, `ORGANISATIONUNIT`, `CITY`, `COUNTRY`, `STREETADDRESS`, `ZIPCODE`, `admin_group`, `admin_user`, `SECTOR_ID`) VALUES(1, 'Default Organization', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

DROP TABLE IF EXISTS `process`;
CREATE TABLE IF NOT EXISTS `process` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `COMPLETENESS` varchar(255) DEFAULT NULL,
  `complianceSystem_cache` varchar(1) DEFAULT NULL,
  `containsProductModel` tinyint(1) DEFAULT '0',
  `EXCHANGESINCLUDED` tinyint(1) DEFAULT '0',
  `FORMAT` varchar(255) DEFAULT NULL,
  `lciMethodInformation_cache` varchar(20) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PARAMETERIZED` tinyint(1) DEFAULT '0',
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `RESULTSINCLUDED` tinyint(1) DEFAULT '0',
  `subtype` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `margins` int(11) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `APPROVEDBY_ID` bigint(20) DEFAULT NULL,
  `OWNERREFERENCE_ID` bigint(20) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `ACCESSINFORMATION_ID` bigint(20) DEFAULT NULL,
  `GEOGRAPHY_ID` bigint(20) DEFAULT NULL,
  `INTERNALREFERENCE_ID` bigint(20) DEFAULT NULL,
  `LCIMETHODINFORMATION_ID` bigint(20) DEFAULT NULL,
  `TIMEINFORMATION_ID` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_process_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_process_GEOGRAPHY_ID` (`GEOGRAPHY_ID`),
  KEY `FK_process_ACCESSINFORMATION_ID` (`ACCESSINFORMATION_ID`),
  KEY `FK_process_APPROVEDBY_ID` (`APPROVEDBY_ID`),
  KEY `FK_process_root_stock_id` (`root_stock_id`),
  KEY `FK_process_LCIMETHODINFORMATION_ID` (`LCIMETHODINFORMATION_ID`),
  KEY `FK_process_XMLFILE_ID` (`XMLFILE_ID`),
  KEY `FK_process_TIMEINFORMATION_ID` (`TIMEINFORMATION_ID`),
  KEY `FK_process_OWNERREFERENCE_ID` (`OWNERREFERENCE_ID`),
  KEY `FK_process_INTERNALREFERENCE_ID` (`INTERNALREFERENCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `processname_base`;
CREATE TABLE IF NOT EXISTS `processname_base` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_processname_base_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `processname_location`;
CREATE TABLE IF NOT EXISTS `processname_location` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_processname_location_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `processname_route`;
CREATE TABLE IF NOT EXISTS `processname_route` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_processname_route_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `processname_unit`;
CREATE TABLE IF NOT EXISTS `processname_unit` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_processname_unit_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_accessinformation`;
CREATE TABLE IF NOT EXISTS `process_accessinformation` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `COPYRIGHT` tinyint(1) DEFAULT '0',
  `LICENSETYPE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `process_classifications`;
CREATE TABLE IF NOT EXISTS `process_classifications` (
  `Process_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Process_ID`,`classifications_ID`),
  KEY `FK_process_classifications_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_compliancesystem`;
CREATE TABLE IF NOT EXISTS `process_compliancesystem` (
  `Process_ID` bigint(20) NOT NULL,
  `complianceSystems_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Process_ID`,`complianceSystems_ID`),
  KEY `FK_process_compliancesystem_complianceSystems_ID` (`complianceSystems_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_description`;
CREATE TABLE IF NOT EXISTS `process_description` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Process_DESCRIPTION_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_exchange`;
CREATE TABLE IF NOT EXISTS `process_exchange` (
  `Process_ID` bigint(20) NOT NULL,
  `exchanges_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Process_ID`,`exchanges_ID`),
  KEY `FK_process_exchange_exchanges_ID` (`exchanges_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_geography`;
CREATE TABLE IF NOT EXISTS `process_geography` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `LOCATION` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `process_lciaresult`;
CREATE TABLE IF NOT EXISTS `process_lciaresult` (
  `Process_ID` bigint(20) NOT NULL,
  `lciaResults_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Process_ID`,`lciaResults_ID`),
  KEY `FK_process_lciaresult_lciaResults_ID` (`lciaResults_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_lcimethodapproaches`;
CREATE TABLE IF NOT EXISTS `process_lcimethodapproaches` (
  `processId` bigint(20) DEFAULT NULL,
  `approach` varchar(255) DEFAULT NULL,
  KEY `FK_process_lcimethodapproaches_processId` (`processId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_locationrestriction`;
CREATE TABLE IF NOT EXISTS `process_locationrestriction` (
  `process_geography_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `process_locationrestriction_process_geography_id` (`process_geography_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_name`;
CREATE TABLE IF NOT EXISTS `process_name` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Process_NAME_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_quantref_otherreference`;
CREATE TABLE IF NOT EXISTS `process_quantref_otherreference` (
  `internalquantitativereference_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `prcssqntrfotherreferencentrnlqntitativereferenceid` (`internalquantitativereference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_quantref_referenceids`;
CREATE TABLE IF NOT EXISTS `process_quantref_referenceids` (
  `internalquantitativereference_id` bigint(20) DEFAULT NULL,
  `refId` int(11) DEFAULT NULL,
  KEY `prcssqntrefreferenceidsntrnlqantitativereferenceid` (`internalquantitativereference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_review`;
CREATE TABLE IF NOT EXISTS `process_review` (
  `Process_ID` bigint(20) NOT NULL,
  `reviews_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Process_ID`,`reviews_ID`),
  KEY `FK_process_review_reviews_ID` (`reviews_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_scenario`;
CREATE TABLE IF NOT EXISTS `process_scenario` (
  `Process_ID` bigint(20) NOT NULL,
  `scenarios_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Process_ID`,`scenarios_ID`),
  KEY `FK_process_scenario_ID` (`scenarios_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_safetymargins_description`;
CREATE TABLE IF NOT EXISTS `process_safetymargins_description` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_process_safetymargins_description_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_synonyms`;
CREATE TABLE IF NOT EXISTS `process_synonyms` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_process_synonyms_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_technicalpurpose`;
CREATE TABLE IF NOT EXISTS `process_technicalpurpose` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_process_technicalpurpose_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_timedescription`;
CREATE TABLE IF NOT EXISTS `process_timedescription` (
  `process_timeinformation_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `process_timedescription_process_timeinformation_id` (`process_timeinformation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_timeinformation`;
CREATE TABLE IF NOT EXISTS `process_timeinformation` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `REFERENCEYEAR` int(11) DEFAULT NULL,
  `VALIDUNTIL` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `process_useadvice`;
CREATE TABLE IF NOT EXISTS `process_useadvice` (
  `process_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_process_useadvice_process_id` (`process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `process_userestrictions`;
CREATE TABLE IF NOT EXISTS `process_userestrictions` (
  `process_accessinformation_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `processuserestrictionsprocess_accessinformation_id` (`process_accessinformation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `quantitativereference`;
CREATE TABLE IF NOT EXISTS `quantitativereference` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `registration_data`;
CREATE TABLE IF NOT EXISTS `registration_data` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NODE_BASEURL` varchar(255) DEFAULT NULL,
  `NODEID` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `registry`;
CREATE TABLE IF NOT EXISTS `registry` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BASEURL` varchar(255) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `STATUS` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `NODE_CREDENTIALS_ID` bigint(20) DEFAULT NULL,
  `REG_DATA_ID` bigint(20) DEFAULT NULL,
  `REGISTRY_CREDENTIALS_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_registry_NODE_CREDENTIALS_ID` (`NODE_CREDENTIALS_ID`),
  KEY `FK_registry_REGISTRY_CREDENTIALS_ID` (`REGISTRY_CREDENTIALS_ID`),
  KEY `FK_registry_REG_DATA_ID` (`REG_DATA_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `review`;
CREATE TABLE IF NOT EXISTS `review` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE` varchar(255) DEFAULT NULL,
  `REFERENCETOREPORT_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_review_REFERENCETOREPORT_ID` (`REFERENCETOREPORT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `review_dataqualityindicator`;
CREATE TABLE IF NOT EXISTS `review_dataqualityindicator` (
  `Review_ID` bigint(20) NOT NULL,
  `qualityIndicators_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Review_ID`,`qualityIndicators_ID`),
  KEY `review_dataqualityindicator_qualityIndicators_ID` (`qualityIndicators_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `review_globalreference`;
CREATE TABLE IF NOT EXISTS `review_globalreference` (
  `Review_ID` bigint(20) NOT NULL,
  `referencesToReviewers_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Review_ID`,`referencesToReviewers_ID`),
  KEY `FK_review_globalreference_referencesToReviewers_ID` (`referencesToReviewers_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `review_methods`;
CREATE TABLE IF NOT EXISTS `review_methods` (
  `scopeofreview_id` bigint(20) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  KEY `FK_review_methods_scopeofreview_id` (`scopeofreview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `review_otherreviewdetails`;
CREATE TABLE IF NOT EXISTS `review_otherreviewdetails` (
  `review_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_review_otherreviewdetails_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `review_reviewdetails`;
CREATE TABLE IF NOT EXISTS `review_reviewdetails` (
  `review_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_review_reviewdetails_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `review_scopeofreview`;
CREATE TABLE IF NOT EXISTS `review_scopeofreview` (
  `Review_ID` bigint(20) NOT NULL,
  `scopes_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Review_ID`,`scopes_ID`),
  KEY `FK_review_scopeofreview_scopes_ID` (`scopes_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `scenario`;
CREATE TABLE IF NOT EXISTS `scenario` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL, 
  `group_` varchar(255) DEFAULT NULL, 
  `default_` boolean DEFAULT NULL, 
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `scenario_description`;
CREATE TABLE IF NOT EXISTS `scenario_description` (
  `scenario_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_scenario_description_scenario_id` (`scenario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `scopeofreview`;
CREATE TABLE IF NOT EXISTS `scopeofreview` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `source`;
CREATE TABLE IF NOT EXISTS `source` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `PUBLICATIONTYPE` varchar(255) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_source_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_source_XMLFILE_ID` (`XMLFILE_ID`),
  KEY `FK_source_root_stock_id` (`root_stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `source_citation`;
CREATE TABLE IF NOT EXISTS `source_citation` (
  `source_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_source_citation_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `source_classifications`;
CREATE TABLE IF NOT EXISTS `source_classifications` (
  `Source_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Source_ID`,`classifications_ID`),
  KEY `FK_source_classifications_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `source_description`;
CREATE TABLE IF NOT EXISTS `source_description` (
  `source_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Source_DESCRIPTION_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `source_globalreference`;
CREATE TABLE IF NOT EXISTS `source_globalreference` (
  `Source_ID` bigint(20) NOT NULL,
  `contacts_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Source_ID`,`contacts_ID`),
  KEY `FK_source_globalreference_contacts_ID` (`contacts_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `source_name`;
CREATE TABLE IF NOT EXISTS `source_name` (
  `source_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_Source_NAME_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `source_shortname`;
CREATE TABLE IF NOT EXISTS `source_shortname` (
  `source_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_source_shortName_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `stock_access_right`;
CREATE TABLE IF NOT EXISTS `stock_access_right` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_right_type` varchar(255) DEFAULT NULL,
  `stock_id` bigint(20) DEFAULT NULL,
  `ug_id` bigint(20) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_stock_access_right_0` (`stock_id`,`access_right_type`,`ug_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

INSERT INTO `stock_access_right` (`ID`, `access_right_type`, `stock_id`, `ug_id`, `value`) VALUES(1, 'User', 1, 0, 9);

DROP TABLE IF EXISTS `t_node_credentials`;
CREATE TABLE IF NOT EXISTS `t_node_credentials` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ACCESSACCOUNT` varchar(255) DEFAULT NULL,
  `ACCESSPASSWORD` longblob,
  `NODEID` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `t_nonce`;
CREATE TABLE IF NOT EXISTS `t_nonce` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USEDATE` datetime DEFAULT NULL,
  `VALUE` longblob,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `t_registry_credentials`;
CREATE TABLE IF NOT EXISTS `t_registry_credentials` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ACCESSACCOUNT` varchar(255) DEFAULT NULL,
  `ACCESSPASSWORD` longblob,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `unit`;
CREATE TABLE IF NOT EXISTS `unit` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `INTERNALID` int(11) DEFAULT NULL,
  `MEANVALUE` double DEFAULT NULL,
  `unitname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `unitgroup`;
CREATE TABLE IF NOT EXISTS `unitgroup` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `referenceUnit_cache` varchar(10) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `REFERENCEUNIT_ID` bigint(20) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_unitgroup_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_unitgroup_REFERENCEUNIT_ID` (`REFERENCEUNIT_ID`),
  KEY `FK_unitgroup_root_stock_id` (`root_stock_id`),
  KEY `FK_unitgroup_XMLFILE_ID` (`XMLFILE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `unitgroup_classifications`;
CREATE TABLE IF NOT EXISTS `unitgroup_classifications` (
  `UnitGroup_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`UnitGroup_ID`,`classifications_ID`),
  KEY `FK_unitgroup_classifications_classifications_ID` (`classifications_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `unitgroup_description`;
CREATE TABLE IF NOT EXISTS `unitgroup_description` (
  `unitgroup_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_UnitGroup_DESCRIPTION_unitgroup_id` (`unitgroup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `unitgroup_name`;
CREATE TABLE IF NOT EXISTS `unitgroup_name` (
  `unitgroup_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_UnitGroup_NAME_unitgroup_id` (`unitgroup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `unitgroup_unit`;
CREATE TABLE IF NOT EXISTS `unitgroup_unit` (
  `UnitGroup_ID` bigint(20) NOT NULL,
  `units_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`UnitGroup_ID`,`units_ID`),
  KEY `FK_unitgroup_unit_units_ID` (`units_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `unit_description`;
CREATE TABLE IF NOT EXISTS `unit_description` (
  `unit_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_unit_description_unit_id` (`unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DSPURPOSE` varchar(255) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `FIRSTNAME` varchar(255) DEFAULT NULL,
  `GENDER` varchar(255) DEFAULT NULL,
  `JOBPOSITION` varchar(255) DEFAULT NULL,
  `LASTNAME` varchar(255) DEFAULT NULL,
  `PASSWORD_HASH` varchar(255) DEFAULT NULL,
  `PASSWORD_HASH_SALT` varchar(255) DEFAULT NULL,
  `REGISTRATIONKEY` varchar(255) DEFAULT NULL,
  `super_admin_permission` tinyint(1) DEFAULT '0',
  `TITLE` varchar(255) DEFAULT NULL,
  `USERNAME` varchar(255) DEFAULT NULL,
  `CITY` varchar(255) DEFAULT NULL,
  `COUNTRY` varchar(255) DEFAULT NULL,
  `STREETADDRESS` varchar(255) DEFAULT NULL,
  `ZIPCODE` varchar(255) DEFAULT NULL,
  `organization` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `USERNAME` (`USERNAME`),
  KEY `FK_user_organization` (`organization`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

INSERT INTO `user` (`ID`, `DSPURPOSE`, `EMAIL`, `FIRSTNAME`, `GENDER`, `JOBPOSITION`, `LASTNAME`, `PASSWORD_HASH`, `PASSWORD_HASH_SALT`, `REGISTRATIONKEY`, `super_admin_permission`, `TITLE`, `USERNAME`, `CITY`, `COUNTRY`, `STREETADDRESS`, `ZIPCODE`, `organization`) VALUES(1, NULL, 'admin@admin.admin', NULL, NULL, NULL, NULL, '8800cc078f01fe328c838ef40245671ae9ddb41ecefc2cff9b25546d0de77d18bdefeb4cdac5e120e094b9b0c9b0439be4166cbd1924d7f9c9ddf2c4b9959473', 'wqugu4btwsqau!6bb6ie', NULL, 1, NULL, 'admin', NULL, NULL, NULL, NULL, 1);

DROP TABLE IF EXISTS `usergroup`;
CREATE TABLE IF NOT EXISTS `usergroup` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `GROUPNAME` varchar(255) NOT NULL,
  `organization` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `GROUPNAME` (`GROUPNAME`),
  KEY `FK_usergroup_organization` (`organization`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=103 ;

INSERT INTO `usergroup` (`ID`, `GROUPNAME`, `organization`) VALUES(2, 'Tools', 1);
INSERT INTO `usergroup` (`ID`, `GROUPNAME`, `organization`) VALUES(102, 'Admin', 1);

DROP TABLE IF EXISTS `usergroup_user`;
CREATE TABLE IF NOT EXISTS `usergroup_user` (
  `users_ID` bigint(20) NOT NULL,
  `groups_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`users_ID`,`groups_ID`),
  KEY `FK_usergroup_user_groups_ID` (`groups_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `usergroup_user` (`users_ID`, `groups_ID`) VALUES(1, 102);

DROP TABLE IF EXISTS `xmlfile`;
CREATE TABLE IF NOT EXISTS `xmlfile` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `COMPRESSEDCONTENT` longblob,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;


ALTER TABLE `categorysystem_category`
  ADD CONSTRAINT `FK_categorysystem_category_CategorySystem_ID` FOREIGN KEY (`CategorySystem_ID`) REFERENCES `categorysystem` (`ID`),
  ADD CONSTRAINT `FK_categorysystem_category_categories_ID` FOREIGN KEY (`categories_ID`) REFERENCES `category` (`ID`);

ALTER TABLE `classification`
  ADD CONSTRAINT `FK_classification_catSystem` FOREIGN KEY (`catSystem`) REFERENCES `categorysystem` (`ID`);

ALTER TABLE `classification_clclass`
  ADD CONSTRAINT `FK_classification_clclass_classes_ID` FOREIGN KEY (`classes_ID`) REFERENCES `clclass` (`ID`),
  ADD CONSTRAINT `FK_classification_clclass_Classification_ID` FOREIGN KEY (`Classification_ID`) REFERENCES `classification` (`ID`);

ALTER TABLE `clclass`
  ADD CONSTRAINT `FK_clclass_CATEGORY_ID` FOREIGN KEY (`CATEGORY_ID`) REFERENCES `category` (`ID`);

ALTER TABLE `compliancesystem`
  ADD CONSTRAINT `FK_compliancesystem_SOURCEREFERENCE_ID` FOREIGN KEY (`SOURCEREFERENCE_ID`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `contact`
  ADD CONSTRAINT `FK_contact_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`),
  ADD CONSTRAINT `FK_contact_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`);

ALTER TABLE `contact_classifications`
  ADD CONSTRAINT `FK_contact_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`),
  ADD CONSTRAINT `FK_contact_classifications_Contact_ID` FOREIGN KEY (`Contact_ID`) REFERENCES `contact` (`ID`);

ALTER TABLE `contact_description`
  ADD CONSTRAINT `FK_Contact_DESCRIPTION_contact_id` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`ID`);

ALTER TABLE `contact_name`
  ADD CONSTRAINT `FK_Contact_NAME_contact_id` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`ID`);

ALTER TABLE `contact_shortname`
  ADD CONSTRAINT `FK_contact_shortname_contact_id` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`ID`);

ALTER TABLE `dataset_registration_data`
  ADD CONSTRAINT `FK_dataset_registration_data_REASON_ID` FOREIGN KEY (`REASON_ID`) REFERENCES `dataset_deregistration_reason` (`ID`),
  ADD CONSTRAINT `FK_dataset_registration_data_REGISTRY_ID` FOREIGN KEY (`REGISTRY_ID`) REFERENCES `registry` (`ID`);

ALTER TABLE `datastock`
  ADD CONSTRAINT `FK_datastock_owner_organization` FOREIGN KEY (`owner_organization`) REFERENCES `organization` (`id`);

ALTER TABLE `datastock_contact`
  ADD CONSTRAINT `FK_datastock_contact_contacts_ID` FOREIGN KEY (`contacts_ID`) REFERENCES `contact` (`ID`),
  ADD CONSTRAINT `FK_datastock_contact_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`);

ALTER TABLE `datastock_description`
  ADD CONSTRAINT `FK_datastock_description_datastock_id` FOREIGN KEY (`datastock_id`) REFERENCES `datastock` (`ID`);

ALTER TABLE `datastock_flowproperty`
  ADD CONSTRAINT `FK_datastock_flowproperty_flowProperties_ID` FOREIGN KEY (`flowProperties_ID`) REFERENCES `flowproperty` (`ID`),
  ADD CONSTRAINT `FK_datastock_flowproperty_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`);

ALTER TABLE `datastock_flow_elementary`
  ADD CONSTRAINT `FK_datastock_flow_elementary_elementaryFlows_ID` FOREIGN KEY (`elementaryFlows_ID`) REFERENCES `flow_common` (`ID`),
  ADD CONSTRAINT `datastock_flow_elementary_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`);

ALTER TABLE `datastock_flow_product`
  ADD CONSTRAINT `FK_datastock_flow_product_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_datastock_flow_product_productFlows_ID` FOREIGN KEY (`productFlows_ID`) REFERENCES `flow_common` (`ID`);

ALTER TABLE `datastock_lciamethod`
  ADD CONSTRAINT `FK_datastock_lciamethod_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_datastock_lciamethod_lciaMethods_ID` FOREIGN KEY (`lciaMethods_ID`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `datastock_longtitle`
  ADD CONSTRAINT `FK_datastock_longtitle_datastock_id` FOREIGN KEY (`datastock_id`) REFERENCES `datastock` (`ID`);

ALTER TABLE `datastock_process`
  ADD CONSTRAINT `FK_datastock_process_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_datastock_process_processes_ID` FOREIGN KEY (`processes_ID`) REFERENCES `process` (`ID`);

ALTER TABLE `datastock_source`
  ADD CONSTRAINT `FK_datastock_source_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_datastock_source_sources_ID` FOREIGN KEY (`sources_ID`) REFERENCES `source` (`ID`);

ALTER TABLE `datastock_unitgroup`
  ADD CONSTRAINT `FK_datastock_unitgroup_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_datastock_unitgroup_unitGroups_ID` FOREIGN KEY (`unitGroups_ID`) REFERENCES `unitgroup` (`ID`);

ALTER TABLE `digitalfile`
  ADD CONSTRAINT `FK_digitalfile_SOURCE_ID` FOREIGN KEY (`SOURCE_ID`) REFERENCES `source` (`ID`);

ALTER TABLE `exchange`
  ADD CONSTRAINT `FK_exchange_FLOWREFERENCE_ID` FOREIGN KEY (`FLOWREFERENCE_ID`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_exchange_FLOW_ID` FOREIGN KEY (`FLOW_ID`) REFERENCES `flow_common` (`ID`),
  ADD CONSTRAINT `FK_exchange_REFTODATASOURCE_ID` FOREIGN KEY (`REFTODATASOURCE_ID`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_exchange_unitgroup_reference` FOREIGN KEY (`unitgroup_reference`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `exchange_amounts`
  ADD CONSTRAINT `FK_Exchange_AMOUNTS_exchange_id` FOREIGN KEY (`exchange_id`) REFERENCES `exchange` (`ID`);

ALTER TABLE `exchange_comment`
  ADD CONSTRAINT `FK_exchange_comment_exchange_id` FOREIGN KEY (`exchange_id`) REFERENCES `exchange` (`ID`);

ALTER TABLE `flowproperty`
  ADD CONSTRAINT `FK_flowproperty_UNITGROUP_ID` FOREIGN KEY (`UNITGROUP_ID`) REFERENCES `unitgroup` (`ID`),
  ADD CONSTRAINT `FK_flowproperty_REFERENCETOUNITGROUP_ID` FOREIGN KEY (`REFERENCETOUNITGROUP_ID`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_flowproperty_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_flowproperty_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`);

ALTER TABLE `flowpropertydescription`
  ADD CONSTRAINT `FK_flowpropertydescription_FLOWPROPERTY_ID` FOREIGN KEY (`FLOWPROPERTY_ID`) REFERENCES `flowproperty` (`ID`),
  ADD CONSTRAINT `FK_flowpropertydescription_FLOWPROPERTYREF_ID` FOREIGN KEY (`FLOWPROPERTYREF_ID`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `flowpropertydescription_description`
  ADD CONSTRAINT `flwprprtydscrptiondescriptionflwprprtydscriptionid` FOREIGN KEY (`flowpropertydescription_id`) REFERENCES `flowpropertydescription` (`ID`);

ALTER TABLE `flowproperty_classifications`
  ADD CONSTRAINT `FK_flowproperty_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`),
  ADD CONSTRAINT `FK_flowproperty_classifications_FlowProperty_ID` FOREIGN KEY (`FlowProperty_ID`) REFERENCES `flowproperty` (`ID`);

ALTER TABLE `flowproperty_description`
  ADD CONSTRAINT `FK_FlowProperty_DESCRIPTION_flowproperty_id` FOREIGN KEY (`flowproperty_id`) REFERENCES `flowproperty` (`ID`);

ALTER TABLE `flowproperty_name`
  ADD CONSTRAINT `FK_FlowProperty_NAME_flowproperty_id` FOREIGN KEY (`flowproperty_id`) REFERENCES `flowproperty` (`ID`);

ALTER TABLE `flowproperty_synonyms`
  ADD CONSTRAINT `FK_flowproperty_synonyms_flowproperty_id` FOREIGN KEY (`flowproperty_id`) REFERENCES `flowproperty` (`ID`);

ALTER TABLE `flow_common`
  ADD CONSTRAINT `FK_flow_common_reference_property_description_id` FOREIGN KEY (`reference_property_description_id`) REFERENCES `flowpropertydescription` (`ID`),
  ADD CONSTRAINT `FK_flow_common_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_flow_common_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`);

ALTER TABLE `flow_common_classification`
  ADD CONSTRAINT `FK_flow_common_classification_Flow_ID` FOREIGN KEY (`Flow_ID`) REFERENCES `flow_common` (`ID`),
  ADD CONSTRAINT `FK_flow_common_classification_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`);

ALTER TABLE `flow_description`
  ADD CONSTRAINT `FK_Flow_DESCRIPTION_Flow_ID` FOREIGN KEY (`Flow_ID`) REFERENCES `flow_common` (`ID`);

ALTER TABLE `flow_elementary`
  ADD CONSTRAINT `FK_flow_elementary_categorization` FOREIGN KEY (`categorization`) REFERENCES `classification` (`ID`),
  ADD CONSTRAINT `FK_flow_elementary_ID` FOREIGN KEY (`ID`) REFERENCES `flow_common` (`ID`);

ALTER TABLE `flow_name`
  ADD CONSTRAINT `FK_Flow_NAME_Flow_ID` FOREIGN KEY (`Flow_ID`) REFERENCES `flow_common` (`ID`);

ALTER TABLE `flow_product`
  ADD CONSTRAINT `FK_flow_product_is_a_reference` FOREIGN KEY (`is_a_reference`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_flow_product_ID` FOREIGN KEY (`ID`) REFERENCES `flow_common` (`ID`),
  ADD CONSTRAINT `FK_flow_product_source_reference` FOREIGN KEY (`source_reference`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_flow_product_vendor_reference` FOREIGN KEY (`vendor_reference`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `flow_propertydescriptions`
  ADD CONSTRAINT `FK_flow_propertydescriptions_Flow_ID` FOREIGN KEY (`Flow_ID`) REFERENCES `flow_common` (`ID`),
  ADD CONSTRAINT `flow_propertydescriptions_propertyDescriptions_ID` FOREIGN KEY (`propertyDescriptions_ID`) REFERENCES `flowpropertydescription` (`ID`);

ALTER TABLE `flow_synonyms`
  ADD CONSTRAINT `FK_flow_synonyms_flow_id` FOREIGN KEY (`flow_id`) REFERENCES `flow_common` (`ID`);

ALTER TABLE `globalreference_shortdescription`
  ADD CONSTRAINT `globalreference_shortdescriptionglobalreference_id` FOREIGN KEY (`globalreference_id`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `globalreference_subreferences`
  ADD CONSTRAINT `globalreference_subreferences_globalreference_id` FOREIGN KEY (`globalreference_id`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `lciamethod`
  ADD CONSTRAINT `FK_lciamethod_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`),
  ADD CONSTRAINT `FK_lciamethod_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`);

ALTER TABLE `lciamethodcharacterisationfactor`
  ADD CONSTRAINT `lcmthodcharacterisationfactorRFRNCEDFLOWINSTANCEID` FOREIGN KEY (`REFERENCEDFLOWINSTANCE_ID`) REFERENCES `flow_common` (`ID`),
  ADD CONSTRAINT `lcmethodcharacterisationfactorFLWGLOBALREFERENCEID` FOREIGN KEY (`FLOWGLOBALREFERENCE_ID`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `lciamethodcharfactor_description`
  ADD CONSTRAINT `lcmthdchrfctrdescriptionlcmthdchrctrsationfactorid` FOREIGN KEY (`lciamethodcharacterisationfactor_id`) REFERENCES `lciamethodcharacterisationfactor` (`ID`);

ALTER TABLE `lciamethod_areaofprotection`
  ADD CONSTRAINT `FK_lciamethod_areaofprotection_lciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_classifications`
  ADD CONSTRAINT `FK_lciamethod_classifications_LCIAMethod_ID` FOREIGN KEY (`LCIAMethod_ID`) REFERENCES `lciamethod` (`ID`),
  ADD CONSTRAINT `FK_lciamethod_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`);

ALTER TABLE `lciamethod_description`
  ADD CONSTRAINT `FK_LCIAMethod_DESCRIPTION_lciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_impactcategory`
  ADD CONSTRAINT `FK_lciamethod_impactcategory_lciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_lciamethodcharacterisationfactor`
  ADD CONSTRAINT `lcmthdlcmthdcharacterisationfactorchrctrstnFctrsID` FOREIGN KEY (`characterisationFactors_ID`) REFERENCES `lciamethodcharacterisationfactor` (`ID`),
  ADD CONSTRAINT `lcmethodlciamethodcharacterisationfactorLCMethodID` FOREIGN KEY (`LCIAMethod_ID`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_methodology`
  ADD CONSTRAINT `FK_lciamethod_methodology_lciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_name`
  ADD CONSTRAINT `FK_LCIAMethod_NAME_lciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_ti_durationdescription`
  ADD CONSTRAINT `FK_lciamethod_ti_durationdescription_lciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciamethod_ti_referenceyeardescription`
  ADD CONSTRAINT `lciamethodti_referenceyeardescriptionlciamethod_id` FOREIGN KEY (`lciamethod_id`) REFERENCES `lciamethod` (`ID`);

ALTER TABLE `lciaresult`
  ADD CONSTRAINT `FK_lciaresult_METHODREFERENCE_ID` FOREIGN KEY (`METHODREFERENCE_ID`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_lciaresult_unitgroup_reference` FOREIGN KEY (`unitgroup_reference`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `lciaresult_amounts`
  ADD CONSTRAINT `FK_LciaResult_AMOUNTS_lciaresult_id` FOREIGN KEY (`lciaresult_id`) REFERENCES `lciaresult` (`ID`);

ALTER TABLE `lciaresult_comment`
  ADD CONSTRAINT `FK_lciaresult_comment_lciaresult_id` FOREIGN KEY (`lciaresult_id`) REFERENCES `lciaresult` (`ID`);

ALTER TABLE `networknode`
  ADD CONSTRAINT `FK_networknode_REGISTRY_ID` FOREIGN KEY (`REGISTRY_ID`) REFERENCES `registry` (`ID`);

ALTER TABLE `organization`
  ADD CONSTRAINT `FK_organization_admin_group` FOREIGN KEY (`admin_group`) REFERENCES `usergroup` (`ID`),
  ADD CONSTRAINT `FK_organization_admin_user` FOREIGN KEY (`admin_user`) REFERENCES `user` (`ID`),
  ADD CONSTRAINT `FK_organization_SECTOR_ID` FOREIGN KEY (`SECTOR_ID`) REFERENCES `industrialsector` (`ID`);

ALTER TABLE `process`
  ADD CONSTRAINT `FK_process_INTERNALREFERENCE_ID` FOREIGN KEY (`INTERNALREFERENCE_ID`) REFERENCES `quantitativereference` (`ID`),
  ADD CONSTRAINT `FK_process_ACCESSINFORMATION_ID` FOREIGN KEY (`ACCESSINFORMATION_ID`) REFERENCES `process_accessinformation` (`ID`),
  ADD CONSTRAINT `FK_process_APPROVEDBY_ID` FOREIGN KEY (`APPROVEDBY_ID`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_process_GEOGRAPHY_ID` FOREIGN KEY (`GEOGRAPHY_ID`) REFERENCES `process_geography` (`ID`),
  ADD CONSTRAINT `FK_process_LCIMETHODINFORMATION_ID` FOREIGN KEY (`LCIMETHODINFORMATION_ID`) REFERENCES `lcimethodinformation` (`ID`),
  ADD CONSTRAINT `FK_process_OWNERREFERENCE_ID` FOREIGN KEY (`OWNERREFERENCE_ID`) REFERENCES `globalreference` (`ID`),
  ADD CONSTRAINT `FK_process_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_process_TIMEINFORMATION_ID` FOREIGN KEY (`TIMEINFORMATION_ID`) REFERENCES `process_timeinformation` (`ID`),
  ADD CONSTRAINT `FK_process_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`);

ALTER TABLE `processname_base`
  ADD CONSTRAINT `FK_processname_base_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `processname_location`
  ADD CONSTRAINT `FK_processname_location_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `processname_route`
  ADD CONSTRAINT `FK_processname_route_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `processname_unit`
  ADD CONSTRAINT `FK_processname_unit_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_classifications`
  ADD CONSTRAINT `FK_process_classifications_Process_ID` FOREIGN KEY (`Process_ID`) REFERENCES `process` (`ID`),
  ADD CONSTRAINT `FK_process_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`);

ALTER TABLE `process_compliancesystem`
  ADD CONSTRAINT `FK_process_compliancesystem_Process_ID` FOREIGN KEY (`Process_ID`) REFERENCES `process` (`ID`),
  ADD CONSTRAINT `FK_process_compliancesystem_complianceSystems_ID` FOREIGN KEY (`complianceSystems_ID`) REFERENCES `compliancesystem` (`ID`);

ALTER TABLE `process_description`
  ADD CONSTRAINT `FK_Process_DESCRIPTION_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_exchange`
  ADD CONSTRAINT `FK_process_exchange_Process_ID` FOREIGN KEY (`Process_ID`) REFERENCES `process` (`ID`),
  ADD CONSTRAINT `FK_process_exchange_exchanges_ID` FOREIGN KEY (`exchanges_ID`) REFERENCES `exchange` (`ID`);

ALTER TABLE `process_lciaresult`
  ADD CONSTRAINT `FK_process_lciaresult_lciaResults_ID` FOREIGN KEY (`lciaResults_ID`) REFERENCES `lciaresult` (`ID`),
  ADD CONSTRAINT `FK_process_lciaresult_Process_ID` FOREIGN KEY (`Process_ID`) REFERENCES `process` (`ID`);

ALTER TABLE `process_lcimethodapproaches`
  ADD CONSTRAINT `FK_process_lcimethodapproaches_processId` FOREIGN KEY (`processId`) REFERENCES `lcimethodinformation` (`ID`);

ALTER TABLE `process_locationrestriction`
  ADD CONSTRAINT `process_locationrestriction_process_geography_id` FOREIGN KEY (`process_geography_id`) REFERENCES `process_geography` (`ID`);

ALTER TABLE `process_name`
  ADD CONSTRAINT `FK_Process_NAME_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_quantref_otherreference`
  ADD CONSTRAINT `prcssqntrfotherreferencentrnlqntitativereferenceid` FOREIGN KEY (`internalquantitativereference_id`) REFERENCES `quantitativereference` (`ID`);

ALTER TABLE `process_quantref_referenceids`
  ADD CONSTRAINT `prcssqntrefreferenceidsntrnlqantitativereferenceid` FOREIGN KEY (`internalquantitativereference_id`) REFERENCES `quantitativereference` (`ID`);

ALTER TABLE `process_review`
  ADD CONSTRAINT `FK_process_review_reviews_ID` FOREIGN KEY (`reviews_ID`) REFERENCES `review` (`ID`),
  ADD CONSTRAINT `FK_process_review_Process_ID` FOREIGN KEY (`Process_ID`) REFERENCES `process` (`ID`);

ALTER TABLE `process_safetymargins_description`
  ADD CONSTRAINT `FK_process_safetymargins_description_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_scenario`
  ADD CONSTRAINT `FK_process_scenario_scenarios_ID` FOREIGN KEY (`scenarios_ID`) REFERENCES `scenario` (`ID`),
  ADD CONSTRAINT `FK_process_scenario_Process_ID` FOREIGN KEY (`Process_ID`) REFERENCES `process` (`ID`);

ALTER TABLE `process_synonyms`
  ADD CONSTRAINT `FK_process_synonyms_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_technicalpurpose`
  ADD CONSTRAINT `FK_process_technicalpurpose_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_timedescription`
  ADD CONSTRAINT `process_timedescription_process_timeinformation_id` FOREIGN KEY (`process_timeinformation_id`) REFERENCES `process_timeinformation` (`ID`);

ALTER TABLE `process_useadvice`
  ADD CONSTRAINT `FK_process_useadvice_process_id` FOREIGN KEY (`process_id`) REFERENCES `process` (`ID`);

ALTER TABLE `process_userestrictions`
  ADD CONSTRAINT `processuserestrictionsprocess_accessinformation_id` FOREIGN KEY (`process_accessinformation_id`) REFERENCES `process_accessinformation` (`ID`);

ALTER TABLE `registry`
  ADD CONSTRAINT `FK_registry_REG_DATA_ID` FOREIGN KEY (`REG_DATA_ID`) REFERENCES `registration_data` (`ID`),
  ADD CONSTRAINT `FK_registry_NODE_CREDENTIALS_ID` FOREIGN KEY (`NODE_CREDENTIALS_ID`) REFERENCES `t_node_credentials` (`ID`),
  ADD CONSTRAINT `FK_registry_REGISTRY_CREDENTIALS_ID` FOREIGN KEY (`REGISTRY_CREDENTIALS_ID`) REFERENCES `t_registry_credentials` (`ID`);

ALTER TABLE `review`
  ADD CONSTRAINT `FK_review_REFERENCETOREPORT_ID` FOREIGN KEY (`REFERENCETOREPORT_ID`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `review_dataqualityindicator`
  ADD CONSTRAINT `review_dataqualityindicator_qualityIndicators_ID` FOREIGN KEY (`qualityIndicators_ID`) REFERENCES `dataqualityindicator` (`ID`),
  ADD CONSTRAINT `FK_review_dataqualityindicator_Review_ID` FOREIGN KEY (`Review_ID`) REFERENCES `review` (`ID`);

ALTER TABLE `review_globalreference`
  ADD CONSTRAINT `FK_review_globalreference_Review_ID` FOREIGN KEY (`Review_ID`) REFERENCES `review` (`ID`),
  ADD CONSTRAINT `FK_review_globalreference_referencesToReviewers_ID` FOREIGN KEY (`referencesToReviewers_ID`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `review_methods`
  ADD CONSTRAINT `FK_review_methods_scopeofreview_id` FOREIGN KEY (`scopeofreview_id`) REFERENCES `scopeofreview` (`ID`);

ALTER TABLE `review_otherreviewdetails`
  ADD CONSTRAINT `FK_review_otherreviewdetails_review_id` FOREIGN KEY (`review_id`) REFERENCES `review` (`ID`);

ALTER TABLE `review_reviewdetails`
  ADD CONSTRAINT `FK_review_reviewdetails_review_id` FOREIGN KEY (`review_id`) REFERENCES `review` (`ID`);

ALTER TABLE `review_scopeofreview`
  ADD CONSTRAINT `FK_review_scopeofreview_scopes_ID` FOREIGN KEY (`scopes_ID`) REFERENCES `scopeofreview` (`ID`),
  ADD CONSTRAINT `FK_review_scopeofreview_Review_ID` FOREIGN KEY (`Review_ID`) REFERENCES `review` (`ID`);

ALTER TABLE `source`
  ADD CONSTRAINT `FK_source_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`),
  ADD CONSTRAINT `FK_source_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`);

ALTER TABLE `source_citation`
  ADD CONSTRAINT `FK_source_citation_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`ID`);

ALTER TABLE `source_classifications`
  ADD CONSTRAINT `FK_source_classifications_Source_ID` FOREIGN KEY (`Source_ID`) REFERENCES `source` (`ID`),
  ADD CONSTRAINT `FK_source_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`);

ALTER TABLE `source_description`
  ADD CONSTRAINT `FK_Source_DESCRIPTION_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`ID`);

ALTER TABLE `source_globalreference`
  ADD CONSTRAINT `FK_source_globalreference_Source_ID` FOREIGN KEY (`Source_ID`) REFERENCES `source` (`ID`),
  ADD CONSTRAINT `FK_source_globalreference_contacts_ID` FOREIGN KEY (`contacts_ID`) REFERENCES `globalreference` (`ID`);

ALTER TABLE `source_name`
  ADD CONSTRAINT `FK_Source_NAME_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`ID`);

ALTER TABLE `source_shortname`
  ADD CONSTRAINT `FK_source_shortName_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`ID`);

ALTER TABLE `unitgroup`
  ADD CONSTRAINT `FK_unitgroup_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`),
  ADD CONSTRAINT `FK_unitgroup_REFERENCEUNIT_ID` FOREIGN KEY (`REFERENCEUNIT_ID`) REFERENCES `unit` (`ID`),
  ADD CONSTRAINT `FK_unitgroup_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`);

ALTER TABLE `unitgroup_classifications`
  ADD CONSTRAINT `FK_unitgroup_classifications_UnitGroup_ID` FOREIGN KEY (`UnitGroup_ID`) REFERENCES `unitgroup` (`ID`),
  ADD CONSTRAINT `FK_unitgroup_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`);

ALTER TABLE `unitgroup_description`
  ADD CONSTRAINT `FK_UnitGroup_DESCRIPTION_unitgroup_id` FOREIGN KEY (`unitgroup_id`) REFERENCES `unitgroup` (`ID`);

ALTER TABLE `unitgroup_name`
  ADD CONSTRAINT `FK_UnitGroup_NAME_unitgroup_id` FOREIGN KEY (`unitgroup_id`) REFERENCES `unitgroup` (`ID`);

ALTER TABLE `unitgroup_unit`
  ADD CONSTRAINT `FK_unitgroup_unit_UnitGroup_ID` FOREIGN KEY (`UnitGroup_ID`) REFERENCES `unitgroup` (`ID`),
  ADD CONSTRAINT `FK_unitgroup_unit_units_ID` FOREIGN KEY (`units_ID`) REFERENCES `unit` (`ID`);

ALTER TABLE `unit_description`
  ADD CONSTRAINT `FK_unit_description_unit_id` FOREIGN KEY (`unit_id`) REFERENCES `unit` (`ID`);

ALTER TABLE `user`
  ADD CONSTRAINT `FK_user_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `usergroup`
  ADD CONSTRAINT `FK_usergroup_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `usergroup_user`
  ADD CONSTRAINT `FK_usergroup_user_users_ID` FOREIGN KEY (`users_ID`) REFERENCES `user` (`ID`),
  ADD CONSTRAINT `FK_usergroup_user_groups_ID` FOREIGN KEY (`groups_ID`) REFERENCES `usergroup` (`ID`);