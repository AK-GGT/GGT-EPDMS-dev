
ALTER TABLE  `contact` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `contact` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion; 


ALTER TABLE  `flowproperty` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `flowproperty` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion; 


ALTER TABLE  `flow_common` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `flow_common` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion; 


ALTER TABLE  `lciamethod` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `lciamethod` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion; 


ALTER TABLE  `process` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `process` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion; 


ALTER TABLE  `source` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `source` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion; 


ALTER TABLE  `unitgroup` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `unitgroup` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion;


ALTER TABLE  `globalreference` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `globalreference` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion;


ALTER TABLE  `dataset_registration_data` ADD `VERSION` int(11) DEFAULT NULL AFTER `SUBMINORVERSION` ;

UPDATE `dataset_registration_data` c SET `VERSION` = c.majorVersion * 100000 + c.minorVersion * 1000 + c.subMinorVersion;



