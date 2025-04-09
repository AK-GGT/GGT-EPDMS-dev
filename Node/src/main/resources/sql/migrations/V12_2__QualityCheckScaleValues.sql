DROP PROCEDURE IF EXISTS calculate_scaled_values_for_batch_qa;

-- this procedure calculates a scaled value for each existing result, based on the amount of the declared unit
DELIMITER $$
CREATE PROCEDURE calculate_scaled_values_for_batch_qa ()
   
BEGIN
	-- row id
	DECLARE lciaresults_id BIGINT(20);
	-- module
	DECLARE module VARCHAR(255);
	-- multiplier value of the declared unit
	DECLARE du_value FLOAT;
    -- indicator value
	DECLARE orig_value FLOAT;

	DECLARE done INT DEFAULT FALSE;

	-- this selects the multiplier (e.meanamount) of the declared unit (e.g. 1000 if the DU is 1000 kg of product)
	DECLARE cur1 CURSOR FOR 
		SELECT la.lciaresult_id, la.module, e.meanamount, la.value
			FROM lciaresult_amounts la
            JOIN `process_lciaresult` pl ON la.lciaresult_id = pl.lciaresults_id
			JOIN `process_exchange` pe ON pe.Process_ID = pl.Process_ID 
            JOIN `exchange` e ON e.ID = pe.exchanges_ID
			JOIN `flow_common` f ON f.ID = e.FLOW_ID
			JOIN `flow_propertydescriptions` fpd ON fpd.Flow_ID = f.ID
			JOIN `flowpropertydescription` fpd2 ON fpd2.ID = fpd.propertyDescriptions_ID;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;
    
    read_loop: LOOP
		FETCH cur1 INTO lciaresults_id, module, du_value, orig_value;
		IF done THEN
		  LEAVE read_loop;
		END IF;

        IF du_value = 1 THEN
			UPDATE lciaresult_amounts la SET la.scaled_value = orig_value WHERE la.lciaresult_id=lciaresults_id AND la.module=module;		
		ELSE
			UPDATE lciaresult_amounts la SET la.scaled_value = orig_value/du_value WHERE la.lciaresult_id=lciaresults_id AND la.module=module;
		END IF;
        
	END LOOP;  
    
    CLOSE cur1;
    
END$$
DELIMITER ;
				
CALL calculate_scaled_values_for_batch_qa;

ALTER TABLE `process_lciaresult_clclass_statistics` ADD `stockIds` varchar(255);
ALTER TABLE `process_lciaresult_clclass_statistics` ADD `reference_count` int(11);
