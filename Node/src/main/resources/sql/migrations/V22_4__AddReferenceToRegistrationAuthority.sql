ALTER TABLE  `process` ADD `registration_authority_reference_ID` bigint(20) DEFAULT NULL;

ALTER TABLE `process`
  ADD CONSTRAINT `FK_process_registration_authority_reference_ID` FOREIGN KEY (`registration_authority_reference_ID`) REFERENCES `globalreference` (`ID`);