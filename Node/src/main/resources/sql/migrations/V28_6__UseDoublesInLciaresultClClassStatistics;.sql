DELETE FROM `process_lciaresult_clclass_statistics`;

ALTER TABLE `process_lciaresult_clclass_statistics`
	MODIFY `val_min` DOUBLE DEFAULT NULL,
	MODIFY `val_max` DOUBLE DEFAULT NULL,
	MODIFY `val_mean` DOUBLE DEFAULT NULL;
	