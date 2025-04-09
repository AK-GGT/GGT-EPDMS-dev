SET FOREIGN_KEY_CHECKS = 0;


-- Table structure for table `LifeCycleModel_DESCRIPTION`
--

DROP TABLE IF EXISTS `LifeCycleModel_DESCRIPTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LifeCycleModel_DESCRIPTION` (
  `LifeCycleModel_ID` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_LifeCycleModel_DESCRIPTION_LifeCycleModel_ID` (`LifeCycleModel_ID`),
  CONSTRAINT `FK_LifeCycleModel_DESCRIPTION_LifeCycleModel_ID` FOREIGN KEY (`LifeCycleModel_ID`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LifeCycleModel_NAME`
--

DROP TABLE IF EXISTS `LifeCycleModel_NAME`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LifeCycleModel_NAME` (
  `LifeCycleModel_ID` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_LifeCycleModel_NAME_LifeCycleModel_ID` (`LifeCycleModel_ID`),
  CONSTRAINT `FK_LifeCycleModel_NAME_LifeCycleModel_ID` FOREIGN KEY (`LifeCycleModel_ID`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Table structure for table `datastock_lifecyclemodel`
--

DROP TABLE IF EXISTS `datastock_lifecyclemodel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datastock_lifecyclemodel` (
  `containingDataStocks_ID` bigint(20) NOT NULL,
  `lifeCycleModels_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`containingDataStocks_ID`,`lifeCycleModels_ID`),
  KEY `FK_datastock_lifecyclemodel_lifeCycleModels_ID` (`lifeCycleModels_ID`),
  CONSTRAINT `FK_datastock_lifecyclemodel_lifeCycleModels_ID` FOREIGN KEY (`lifeCycleModels_ID`) REFERENCES `lifecyclemodel` (`ID`),
  CONSTRAINT `datastock_lifecyclemodel_containingDataStocks_ID` FOREIGN KEY (`containingDataStocks_ID`) REFERENCES `datastock` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Table structure for table `downstreamprocess`
--

DROP TABLE IF EXISTS `downstreamprocess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `downstreamprocess` (
  `downstreamprocess_key` bigint(20) NOT NULL AUTO_INCREMENT,
  `dominant` tinyint(1) DEFAULT '0',
  `flowUUID` varchar(255) DEFAULT NULL,
  `internaldownstreamprocess_id` bigint(20) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`downstreamprocess_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Table structure for table `lcm_administrativeinformation`
--

DROP TABLE IF EXISTS `lcm_administrativeinformation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_administrativeinformation` (
  `administrativeinformation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `CommissionerAndGoal_id` bigint(20) DEFAULT NULL,
  `DataEntryBy_id` bigint(20) DEFAULT NULL,
  `PublicationAndOwnership_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`administrativeinformation_id`),
  KEY `FK_lcm_administrativeinformation_DataEntryBy_id` (`DataEntryBy_id`),
  KEY `lcmdministrativeinformationPblcationAndOwnershipid` (`PublicationAndOwnership_id`),
  KEY `lcmadministrativeinformationCommissionerAndGoal_id` (`CommissionerAndGoal_id`),
  CONSTRAINT `FK_lcm_administrativeinformation_DataEntryBy_id` FOREIGN KEY (`DataEntryBy_id`) REFERENCES `lcm_administrativeinformation_dataentryby` (`dataentryby_id`),
  CONSTRAINT `lcmadministrativeinformationCommissionerAndGoal_id` FOREIGN KEY (`CommissionerAndGoal_id`) REFERENCES `lcm_administrativeinformation_commissionerandgoal` (`CommissionerAndGoal_id`),
  CONSTRAINT `lcmdministrativeinformationPblcationAndOwnershipid` FOREIGN KEY (`PublicationAndOwnership_id`) REFERENCES `lcm_administrativeinformation_publicationandownership` (`PublicationAndOwnership_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_administrativeinformation_commissionerandgoal`
--

DROP TABLE IF EXISTS `lcm_administrativeinformation_commissionerandgoal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_administrativeinformation_commissionerandgoal` (
  `CommissionerAndGoal_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `USELESSCOLUMN` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`CommissionerAndGoal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_administrativeinformation_dataentryby`
--

DROP TABLE IF EXISTS `lcm_administrativeinformation_dataentryby`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_administrativeinformation_dataentryby` (
  `dataentryby_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`dataentryby_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_administrativeinformation_publicationandownership`
--

DROP TABLE IF EXISTS `lcm_administrativeinformation_publicationandownership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_administrativeinformation_publicationandownership` (
  `PublicationAndOwnership_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `COPYRIGHT` tinyint(1) DEFAULT '0',
  `LICENSETYPE` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  `REFERENCETOOWNERSHIPOFDATASET_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PublicationAndOwnership_id`),
  KEY `lcmdmnstrtvnfrmtnpblctnndwnrshipRFRNCTWNRSHPFDTSTD` (`REFERENCETOOWNERSHIPOFDATASET_ID`),
  CONSTRAINT `lcmdmnstrtvnfrmtnpblctnndwnrshipRFRNCTWNRSHPFDTSTD` FOREIGN KEY (`REFERENCETOOWNERSHIPOFDATASET_ID`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_administrativeinformation_ref_generator`
--

DROP TABLE IF EXISTS `lcm_administrativeinformation_ref_generator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_administrativeinformation_ref_generator` (
  `administrativeinformation_id` bigint(20) NOT NULL,
  `ref_contact_id` bigint(20) NOT NULL,
  PRIMARY KEY (`administrativeinformation_id`,`ref_contact_id`),
  KEY `lcmdministrativeinformationrefgeneratorrfcontactid` (`ref_contact_id`),
  CONSTRAINT `lcmdministrativeinformationrefgeneratorrfcontactid` FOREIGN KEY (`ref_contact_id`) REFERENCES `globalreference` (`ID`),
  CONSTRAINT `lcmdmnstrtvnfrmationrefgeneratordmnstrtvnfrmtionid` FOREIGN KEY (`administrativeinformation_id`) REFERENCES `lcm_administrativeinformation` (`administrativeinformation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_commissionerandgoal_intendedapplications`
--

DROP TABLE IF EXISTS `lcm_commissionerandgoal_intendedapplications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_commissionerandgoal_intendedapplications` (
  `CommissionerAndGoal_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lcmcmmssnrndgoalintendedapplicationsCmmssnrndGalid` (`CommissionerAndGoal_id`),
  CONSTRAINT `lcmcmmssnrndgoalintendedapplicationsCmmssnrndGalid` FOREIGN KEY (`CommissionerAndGoal_id`) REFERENCES `lcm_administrativeinformation_commissionerandgoal` (`CommissionerAndGoal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_commissionerandgoal_project`
--

DROP TABLE IF EXISTS `lcm_commissionerandgoal_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_commissionerandgoal_project` (
  `CommissionerAndGoal_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lcmcommissionerandgoalprojectCommissionerAndGoalid` (`CommissionerAndGoal_id`),
  CONSTRAINT `lcmcommissionerandgoalprojectCommissionerAndGoalid` FOREIGN KEY (`CommissionerAndGoal_id`) REFERENCES `lcm_administrativeinformation_commissionerandgoal` (`CommissionerAndGoal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_commissionerandgoal_referencetocommissioner`
--

DROP TABLE IF EXISTS `lcm_commissionerandgoal_referencetocommissioner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_commissionerandgoal_referencetocommissioner` (
  `CommissionerAndGoal_id` bigint(20) NOT NULL,
  `ref_contact_id` bigint(20) NOT NULL,
  PRIMARY KEY (`CommissionerAndGoal_id`,`ref_contact_id`),
  KEY `lcmcmmssonerandgoalreferencetocommissionerrfcntctd` (`ref_contact_id`),
  CONSTRAINT `lcmcmmssnrndgalreferencetocommissionerCmmssnrndGld` FOREIGN KEY (`CommissionerAndGoal_id`) REFERENCES `lcm_administrativeinformation_commissionerandgoal` (`CommissionerAndGoal_id`),
  CONSTRAINT `lcmcmmssonerandgoalreferencetocommissionerrfcntctd` FOREIGN KEY (`ref_contact_id`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_dataentry_ref_datasetformat`
--

DROP TABLE IF EXISTS `lcm_dataentry_ref_datasetformat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_dataentry_ref_datasetformat` (
  `dataentryby_id` bigint(20) NOT NULL,
  `ref_source_id` bigint(20) NOT NULL,
  PRIMARY KEY (`dataentryby_id`,`ref_source_id`),
  KEY `FK_lcm_dataentry_ref_datasetformat_ref_source_id` (`ref_source_id`),
  CONSTRAINT `FK_lcm_dataentry_ref_datasetformat_dataentryby_id` FOREIGN KEY (`dataentryby_id`) REFERENCES `lcm_administrativeinformation_dataentryby` (`dataentryby_id`),
  CONSTRAINT `FK_lcm_dataentry_ref_datasetformat_ref_source_id` FOREIGN KEY (`ref_source_id`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_dataentry_ref_entity_entr_data`
--

DROP TABLE IF EXISTS `lcm_dataentry_ref_entity_entr_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_dataentry_ref_entity_entr_data` (
  `dataentryby_id` bigint(20) NOT NULL,
  `ref_contact_id` bigint(20) NOT NULL,
  PRIMARY KEY (`dataentryby_id`,`ref_contact_id`),
  KEY `lcm_dataentry_ref_entity_entr_data_ref_contact_id` (`ref_contact_id`),
  CONSTRAINT `lcm_dataentry_ref_entity_entr_data_dataentryby_id` FOREIGN KEY (`dataentryby_id`) REFERENCES `lcm_administrativeinformation_dataentryby` (`dataentryby_id`),
  CONSTRAINT `lcm_dataentry_ref_entity_entr_data_ref_contact_id` FOREIGN KEY (`ref_contact_id`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_publicationandownership_ref_datasetversion`
--

DROP TABLE IF EXISTS `lcm_publicationandownership_ref_datasetversion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_publicationandownership_ref_datasetversion` (
  `PublicationAndOwnership_id` bigint(20) NOT NULL,
  `ref_precedingDataSetVersion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`PublicationAndOwnership_id`,`ref_precedingDataSetVersion_id`),
  KEY `lcmpblctnndwnrshprfdatasetversionrfprcdngDtStVrsnd` (`ref_precedingDataSetVersion_id`),
  CONSTRAINT `lcmpblctnndwnrshiprefdatasetversionPblctnndwnrshpd` FOREIGN KEY (`PublicationAndOwnership_id`) REFERENCES `lcm_administrativeinformation_publicationandownership` (`PublicationAndOwnership_id`),
  CONSTRAINT `lcmpblctnndwnrshprfdatasetversionrfprcdngDtStVrsnd` FOREIGN KEY (`ref_precedingDataSetVersion_id`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_publicationandownership_ref_entities_w_exc_access`
--

DROP TABLE IF EXISTS `lcm_publicationandownership_ref_entities_w_exc_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_publicationandownership_ref_entities_w_exc_access` (
  `PublicationAndOwnership_id` bigint(20) NOT NULL,
  `ref_entities_w_exc_access_id` bigint(20) NOT NULL,
  PRIMARY KEY (`PublicationAndOwnership_id`,`ref_entities_w_exc_access_id`),
  KEY `lcmpblctnndwnrshprfntitieswexcaccessrfnttswxcccssd` (`ref_entities_w_exc_access_id`),
  CONSTRAINT `lcmpblctnndwnrshprfntitieswexcaccessrfnttswxcccssd` FOREIGN KEY (`ref_entities_w_exc_access_id`) REFERENCES `globalreference` (`ID`),
  CONSTRAINT `lcmpblctnndwnrshprfnttieswexcaccessPblctnndwnrshpd` FOREIGN KEY (`PublicationAndOwnership_id`) REFERENCES `lcm_administrativeinformation_publicationandownership` (`PublicationAndOwnership_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lcm_publicationandownership_userestrictions`
--

DROP TABLE IF EXISTS `lcm_publicationandownership_userestrictions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lcm_publicationandownership_userestrictions` (
  `PublicationAndOwnership_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lcmpblctnndwnrshipuserestrictionsPblctnndwnrshipid` (`PublicationAndOwnership_id`),
  CONSTRAINT `lcmpblctnndwnrshipuserestrictionsPblctnndwnrshipid` FOREIGN KEY (`PublicationAndOwnership_id`) REFERENCES `lcm_administrativeinformation_publicationandownership` (`PublicationAndOwnership_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel`
--

DROP TABLE IF EXISTS `lifecyclemodel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BRANCH` int(11) DEFAULT NULL,
  `classification_cache` varchar(100) DEFAULT NULL,
  `IMPORTDATE` datetime DEFAULT NULL,
  `MOSTRECENTVERSION` tinyint(1) DEFAULT '0',
  `name_cache` varchar(255) DEFAULT NULL,
  `PERMANENTURI` varchar(255) DEFAULT NULL,
  `ref_ref_process` bigint(20) DEFAULT NULL,
  `RELEASESTATE` varchar(255) DEFAULT NULL,
  `UUID` varchar(255) DEFAULT NULL,
  `MAJORVERSION` int(11) DEFAULT NULL,
  `MINORVERSION` int(11) DEFAULT NULL,
  `SUBMINORVERSION` int(11) DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  `root_stock_id` bigint(20) DEFAULT NULL,
  `XMLFILE_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNQ_lifecyclemodel_0` (`UUID`,`MAJORVERSION`,`MINORVERSION`,`SUBMINORVERSION`),
  KEY `FK_lifecyclemodel_root_stock_id` (`root_stock_id`),
  KEY `FK_lifecyclemodel_XMLFILE_ID` (`XMLFILE_ID`),
  CONSTRAINT `FK_lifecyclemodel_XMLFILE_ID` FOREIGN KEY (`XMLFILE_ID`) REFERENCES `xmlfile` (`ID`),
  CONSTRAINT `FK_lifecyclemodel_root_stock_id` FOREIGN KEY (`root_stock_id`) REFERENCES `datastock` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_administrativeinformation`
--

DROP TABLE IF EXISTS `lifecyclemodel_administrativeinformation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_administrativeinformation` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `administrativeinformation_id` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`administrativeinformation_id`),
  KEY `lfcyclmdldmnstrativeinformationdmnstrtvnfrmationid` (`administrativeinformation_id`),
  CONSTRAINT `lfcyclmdldmnstrativeinformationdmnstrtvnfrmationid` FOREIGN KEY (`administrativeinformation_id`) REFERENCES `lcm_administrativeinformation` (`administrativeinformation_id`),
  CONSTRAINT `lfcyclmodeladministrativeinformationlfcyclemodelid` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_classifications`
--

DROP TABLE IF EXISTS `lifecyclemodel_classifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_classifications` (
  `LifeCycleModel_ID` bigint(20) NOT NULL,
  `classifications_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`LifeCycleModel_ID`,`classifications_ID`),
  KEY `lifecyclemodel_classifications_classifications_ID` (`classifications_ID`),
  CONSTRAINT `lifecyclemodel_classifications_LifeCycleModel_ID` FOREIGN KEY (`LifeCycleModel_ID`) REFERENCES `lifecyclemodel` (`ID`),
  CONSTRAINT `lifecyclemodel_classifications_classifications_ID` FOREIGN KEY (`classifications_ID`) REFERENCES `classification` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_compliancesystem`
--

DROP TABLE IF EXISTS `lifecyclemodel_compliancesystem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_compliancesystem` (
  `LifeCycleModel_ID` bigint(20) NOT NULL,
  `complianceSystems_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`LifeCycleModel_ID`,`complianceSystems_ID`),
  KEY `lifecyclemodelcompliancesystemcomplianceSystems_ID` (`complianceSystems_ID`),
  CONSTRAINT `lifecyclemodel_compliancesystem_LifeCycleModel_ID` FOREIGN KEY (`LifeCycleModel_ID`) REFERENCES `lifecyclemodel` (`ID`),
  CONSTRAINT `lifecyclemodelcompliancesystemcomplianceSystems_ID` FOREIGN KEY (`complianceSystems_ID`) REFERENCES `compliancesystem` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_dataSourcestreatmentetc`
--

DROP TABLE IF EXISTS `lifecyclemodel_dataSourcestreatmentetc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_dataSourcestreatmentetc` (
  `lifecyclemodel_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lfcyclemodeldataSourcestreatmentetclfecyclemodelid` (`lifecyclemodel_id`),
  CONSTRAINT `lfcyclemodeldataSourcestreatmentetclfecyclemodelid` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_datasetinformation_basename`
--

DROP TABLE IF EXISTS `lifecyclemodel_datasetinformation_basename`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_datasetinformation_basename` (
  `lifecyclemodel_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lfcyclmodeldatasetinformationbasenamelfcyclmodelid` (`lifecyclemodel_id`),
  CONSTRAINT `lfcyclmodeldatasetinformationbasenamelfcyclmodelid` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_datasetinformation_functionalunitflowproperties`
--

DROP TABLE IF EXISTS `lifecyclemodel_datasetinformation_functionalunitflowproperties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_datasetinformation_functionalunitflowproperties` (
  `lifecyclemodel_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lfcyclmdldtstnfrmtnfnctnlntflwpropertieslfcyclmdld` (`lifecyclemodel_id`),
  CONSTRAINT `lfcyclmdldtstnfrmtnfnctnlntflwpropertieslfcyclmdld` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_datasetinformation_generalcomment`
--

DROP TABLE IF EXISTS `lifecyclemodel_datasetinformation_generalcomment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_datasetinformation_generalcomment` (
  `lifecyclemodel_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lfcyclmdldtasetinformationgeneralcommentlfcyclmdld` (`lifecyclemodel_id`),
  CONSTRAINT `lfcyclmdldtasetinformationgeneralcommentlfcyclmdld` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_datasetinformation_mixandlocationTypes`
--

DROP TABLE IF EXISTS `lifecyclemodel_datasetinformation_mixandlocationTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_datasetinformation_mixandlocationTypes` (
  `lifecyclemodel_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lfcyclmdldtstnfrmtionmixandlocationTypeslfcyclmdld` (`lifecyclemodel_id`),
  CONSTRAINT `lfcyclmdldtstnfrmtionmixandlocationTypeslfcyclmdld` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_datasetinformation_treatmentstandardsroutes`
--

DROP TABLE IF EXISTS `lifecyclemodel_datasetinformation_treatmentstandardsroutes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_datasetinformation_treatmentstandardsroutes` (
  `lifecyclemodel_id` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `lfcyclmdldtstnfrmtntrtmntstandardsrouteslfcyclmdld` (`lifecyclemodel_id`),
  CONSTRAINT `lfcyclmdldtstnfrmtntrtmntstandardsrouteslfcyclmdld` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_diagram`
--

DROP TABLE IF EXISTS `lifecyclemodel_diagram`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_diagram` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `globalreferenceid` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`globalreferenceid`),
  KEY `FK_lifecyclemodel_diagram_globalreferenceid` (`globalreferenceid`),
  CONSTRAINT `FK_lifecyclemodel_diagram_globalreferenceid` FOREIGN KEY (`globalreferenceid`) REFERENCES `globalreference` (`ID`),
  CONSTRAINT `FK_lifecyclemodel_diagram_lifecyclemodel_id` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_languages`
--

DROP TABLE IF EXISTS `lifecyclemodel_languages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_languages` (
  `LifeCycleModel_ID` bigint(20) NOT NULL,
  `supportedLanguages_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`LifeCycleModel_ID`,`supportedLanguages_ID`),
  KEY `FK_lifecyclemodel_languages_supportedLanguages_ID` (`supportedLanguages_ID`),
  CONSTRAINT `FK_lifecyclemodel_languages_LifeCycleModel_ID` FOREIGN KEY (`LifeCycleModel_ID`) REFERENCES `lifecyclemodel` (`ID`),
  CONSTRAINT `FK_lifecyclemodel_languages_supportedLanguages_ID` FOREIGN KEY (`supportedLanguages_ID`) REFERENCES `languages` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_processinstance`
--

DROP TABLE IF EXISTS `lifecyclemodel_processinstance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_processinstance` (
  `processinstance_key` bigint(20) NOT NULL AUTO_INCREMENT,
  `datasetinternal_id` bigint(20) DEFAULT NULL,
  `multiplicationfactor` double NOT NULL,
  `scalingfactor` double DEFAULT NULL,
  `referenceToProcess` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`processinstance_key`),
  KEY `lifecyclemodel_processinstance_referenceToProcess` (`referenceToProcess`),
  CONSTRAINT `lifecyclemodel_processinstance_referenceToProcess` FOREIGN KEY (`referenceToProcess`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_referencetoexternaldocmentation`
--

DROP TABLE IF EXISTS `lifecyclemodel_referencetoexternaldocmentation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_referencetoexternaldocmentation` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `globalreferenceid` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`globalreferenceid`),
  KEY `lfcyclmdlrferencetoexternaldocmentationglblrfrncid` (`globalreferenceid`),
  CONSTRAINT `lfcyclmdlreferencetoexternaldocmentationlfcyclmdld` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`),
  CONSTRAINT `lfcyclmdlrferencetoexternaldocmentationglblrfrncid` FOREIGN KEY (`globalreferenceid`) REFERENCES `globalreference` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_referencetoresultingprocess`
--

DROP TABLE IF EXISTS `lifecyclemodel_referencetoresultingprocess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_referencetoresultingprocess` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `globalreferenceid` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`globalreferenceid`),
  KEY `lfcyclmdelreferencetoresultingprocessglblrfrenceid` (`globalreferenceid`),
  CONSTRAINT `lfcyclmdelreferencetoresultingprocessglblrfrenceid` FOREIGN KEY (`globalreferenceid`) REFERENCES `globalreference` (`ID`),
  CONSTRAINT `lfcyclmdelreferencetoresultingprocesslfcyclmodelid` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_reviews`
--

DROP TABLE IF EXISTS `lifecyclemodel_reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_reviews` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `review_id` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`review_id`),
  KEY `FK_lifecyclemodel_reviews_review_id` (`review_id`),
  CONSTRAINT `FK_lifecyclemodel_reviews_lifecyclemodel_id` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`),
  CONSTRAINT `FK_lifecyclemodel_reviews_review_id` FOREIGN KEY (`review_id`) REFERENCES `review` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_technology_groups`
--

DROP TABLE IF EXISTS `lifecyclemodel_technology_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_technology_groups` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `techgroup_key` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`techgroup_key`),
  KEY `FK_lifecyclemodel_technology_groups_techgroup_key` (`techgroup_key`),
  CONSTRAINT `FK_lifecyclemodel_technology_groups_techgroup_key` FOREIGN KEY (`techgroup_key`) REFERENCES `technology_group` (`techgroup_key`),
  CONSTRAINT `lifecyclemodel_technology_groups_lifecyclemodel_id` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lifecyclemodel_technology_processes`
--

DROP TABLE IF EXISTS `lifecyclemodel_technology_processes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lifecyclemodel_technology_processes` (
  `lifecyclemodel_id` bigint(20) NOT NULL,
  `processinstance_key` bigint(20) NOT NULL,
  PRIMARY KEY (`lifecyclemodel_id`,`processinstance_key`),
  KEY `lfecyclemodeltechnologyprocessesprocessinstancekey` (`processinstance_key`),
  CONSTRAINT `lfecyclemodeltechnologyprocessesprocessinstancekey` FOREIGN KEY (`processinstance_key`) REFERENCES `lifecyclemodel_processinstance` (`processinstance_key`),
  CONSTRAINT `lifecyclemodeltechnologyprocesseslifecyclemodel_id` FOREIGN KEY (`lifecyclemodel_id`) REFERENCES `lifecyclemodel` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Table structure for table `outputexchange`
--

DROP TABLE IF EXISTS `outputexchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outputexchange` (
  `outputexchange_key` bigint(20) NOT NULL AUTO_INCREMENT,
  `dominant` tinyint(1) DEFAULT '0',
  `flowUUID` varchar(255) DEFAULT NULL,
  `internaloutputexchange_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`outputexchange_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `outputexchange_downstreamprocess`
--

DROP TABLE IF EXISTS `outputexchange_downstreamprocess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outputexchange_downstreamprocess` (
  `outputexchange_key` bigint(20) NOT NULL,
  `downstreamprocess_key` bigint(20) NOT NULL,
  PRIMARY KEY (`outputexchange_key`,`downstreamprocess_key`),
  KEY `utputexchangedownstreamprocessdownstreamprocesskey` (`downstreamprocess_key`),
  CONSTRAINT `outputexchange_downstreamprocessoutputexchange_key` FOREIGN KEY (`outputexchange_key`) REFERENCES `outputexchange` (`outputexchange_key`),
  CONSTRAINT `utputexchangedownstreamprocessdownstreamprocesskey` FOREIGN KEY (`downstreamprocess_key`) REFERENCES `downstreamprocess` (`downstreamprocess_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Table structure for table `processinstance_connections`
--

DROP TABLE IF EXISTS `processinstance_connections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processinstance_connections` (
  `processinstanceid` bigint(20) NOT NULL,
  `outputexchange_id` bigint(20) NOT NULL,
  PRIMARY KEY (`processinstanceid`,`outputexchange_id`),
  KEY `FK_processinstance_connections_outputexchange_id` (`outputexchange_id`),
  CONSTRAINT `FK_processinstance_connections_outputexchange_id` FOREIGN KEY (`outputexchange_id`) REFERENCES `outputexchange` (`outputexchange_key`),
  CONSTRAINT `FK_processinstance_connections_processinstanceid` FOREIGN KEY (`processinstanceid`) REFERENCES `lifecyclemodel_processinstance` (`processinstance_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `processinstance_memberofgroup`
--

DROP TABLE IF EXISTS `processinstance_memberofgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processinstance_memberofgroup` (
  `processinstance_key` bigint(20) DEFAULT NULL,
  `memberof_group_id` int(11) DEFAULT NULL,
  KEY `processinstance_memberofgroup_processinstance_key` (`processinstance_key`),
  CONSTRAINT `processinstance_memberofgroup_processinstance_key` FOREIGN KEY (`processinstance_key`) REFERENCES `lifecyclemodel_processinstance` (`processinstance_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `processinstance_parameters`
--

DROP TABLE IF EXISTS `processinstance_parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processinstance_parameters` (
  `processinstance_key` bigint(20) DEFAULT NULL,
  `parameter_value` text,
  `parameter_MatV` varchar(255) DEFAULT NULL,
  KEY `FK_processinstance_parameters_processinstance_key` (`processinstance_key`),
  CONSTRAINT `FK_processinstance_parameters_processinstance_key` FOREIGN KEY (`processinstance_key`) REFERENCES `lifecyclemodel_processinstance` (`processinstance_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- Table structure for table `technology_group`
--

DROP TABLE IF EXISTS `technology_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `technology_group` (
  `techgroup_key` bigint(20) NOT NULL AUTO_INCREMENT,
  `internalgroup_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`techgroup_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `technology_groups_details`
--

DROP TABLE IF EXISTS `technology_groups_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `technology_groups_details` (
  `techgroupkey` bigint(20) DEFAULT NULL,
  `value` text,
  `lang` varchar(255) DEFAULT NULL,
  KEY `FK_technology_groups_details_techgroupkey` (`techgroupkey`),
  CONSTRAINT `FK_technology_groups_details_techgroupkey` FOREIGN KEY (`techgroupkey`) REFERENCES `technology_group` (`techgroup_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


SET FOREIGN_KEY_CHECKS = 1;