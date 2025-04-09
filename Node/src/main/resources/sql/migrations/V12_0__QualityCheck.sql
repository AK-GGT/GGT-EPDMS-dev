DELETE FROM `process_lciaresult_clclass_statistics`;

ALTER TABLE `process_lciaresult_clclass_statistics`
DROP `unitgroup_uuid`;

ALTER TABLE `process_lciaresult_clclass_statistics`
ADD `method_uuid` VARCHAR(255);

ALTER TABLE `process_lciaresult_clclass_statistics`
ADD `reference_flowproperty_uuid` VARCHAR(255);

ALTER TABLE `process_lciaresult_clclass_statistics`
ADD `ref_unit` VARCHAR(255);

ALTER TABLE `process_lciaresult_clclass_statistics` ADD KEY `method_uuid` (`method_uuid`);
ALTER TABLE `process_lciaresult_clclass_statistics` ADD KEY `reference_flowproperty_uuid` (`reference_flowproperty_uuid`);

ALTER TABLE `lciaresult_amounts` ADD `scaled_value` float DEFAULT NULL;

ALTER TABLE `exchange_amounts` ADD `scaled_value` float DEFAULT NULL;
