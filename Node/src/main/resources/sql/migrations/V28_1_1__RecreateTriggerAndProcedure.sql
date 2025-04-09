-- re-add trigger and procedure just in case they have not been exported during database dump, which by default they're not

DROP TRIGGER IF EXISTS after_process_lciaresult_insert;

-- this trigger calculates a scaled value for each result, based on the amount of the declared unit
DELIMITER $$
CREATE TRIGGER after_process_lciaresult_insert
    AFTER INSERT ON process_lciaresult
    FOR EACH ROW
BEGIN
    -- multiplier value of the declared unit
    DECLARE du_value FLOAT;
    -- indicator value
	DECLARE orig_value FLOAT;

	DECLARE done INT DEFAULT FALSE;

	-- this selects the multiplier (e.meanamount) of the declared unit (e.g. 1000 if the DU is 1000 kg of product)
	DECLARE cur1 CURSOR FOR
    SELECT e.meanamount, la.value
    FROM `exchange` e
             JOIN `process_exchange` pe ON e.ID = pe.exchanges_ID
             JOIN `process_lciaresult` pl ON pe.Process_ID = pl.Process_ID
             JOIN `flow_common` f ON f.ID = e.FLOW_ID
             JOIN `flow_propertydescriptions` fpd ON fpd.Flow_ID = f.ID
             JOIN `flowpropertydescription` fpd2 ON fpd2.ID = fpd.propertyDescriptions_ID
             JOIN lciaresult_amounts la ON la.lciaresult_id = pl.lciaresults_id
    WHERE pl.lciaresults_id = NEW.lciaresults_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
		FETCH cur1 INTO du_value, orig_value;
		IF done THEN
		  LEAVE read_loop;
END IF;

IF du_value = 1 THEN
UPDATE lciaresult_amounts SET scaled_value = orig_value WHERE lciaresult_id=NEW.lciaresults_id;
ELSE
UPDATE lciaresult_amounts SET scaled_value = orig_value/du_value WHERE lciaresult_id=NEW.lciaresults_id;
END IF;

END LOOP;

CLOSE cur1;

END$$
DELIMITER ;



DROP PROCEDURE IF EXISTS calculate_scaled_values_for_batch_qa;

-- this procedure calculates a scaled value for each existing result, based on the amount of the declared unit
DELIMITER $$
CREATE PROCEDURE calculate_scaled_values_for_batch_qa () SQL SECURITY INVOKER

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
