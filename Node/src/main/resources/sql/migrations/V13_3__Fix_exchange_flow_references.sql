DROP PROCEDURE IF EXISTS fix_exchange_flow_references;

-- this procedure repairs wrong connections of flow objects to exchanges 
DELIMITER $$
CREATE PROCEDURE fix_exchange_flow_references ()
   
BEGIN
	DECLARE exchange_id BIGINT(20);
	DECLARE flow_id BIGINT(20);
	DECLARE flowreference_id BIGINT(20);
    
    DECLARE flow_version_maj INT(11);
	DECLARE flow_version_min INT(11);
	DECLARE flow_version_sub INT(11);

    DECLARE flowreference_uuid VARCHAR(255);

	DECLARE flowreference_version_maj INT(11);
	DECLARE flowreference_version_min INT(11);
	DECLARE flowreference_version_sub INT(11);

	DECLARE correct_flow_id BIGINT(20);

	DECLARE done INT DEFAULT FALSE;

	DECLARE cur1 
		CURSOR FOR 
			SELECT e.id, e.flow_id, e.flowreference_id, f.MAJORVERSION, f.MINORVERSION, f.SUBMINORVERSION, g.MAJORVERSION, g.MINORVERSION, g.SUBMINORVERSION  FROM exchange e 
				LEFT JOIN flow_common f ON f.ID = e.FLOW_ID
				LEFT JOIN globalreference g ON g.ID = e.FLOWREFERENCE_ID
				WHERE NOT(f.MAJORVERSION = g.MAJORVERSION 
					AND f.MINORVERSION = g.MAJORVERSION
					AND f.SUBMINORVERSION = g.SUBMINORVERSION);    

    DECLARE CONTINUE HANDLER
		FOR NOT FOUND SET done = TRUE;

    OPEN cur1;
    
    read_loop: LOOP
		FETCH cur1 INTO exchange_id, flow_id, flowreference_id, flow_version_maj, flow_version_min, flow_version_sub, flowreference_version_maj, flowreference_version_min, flowreference_version_sub;
		IF done THEN
		  LEAVE read_loop;
		END IF;

		-- and update it on the exchange
		UPDATE exchange e SET e.FLOW_ID = (
        		SELECT f.id FROM flow_common f 
			WHERE f.UUID = flowreference_uuid 
				AND f.MAJORVERSION = flowreference_version_maj 
				AND f.MINORVERSION = flowreference_version_min 
				AND f.SUBMINORVERSION = flowreference_version_sub
        ) WHERE e.id = exchange_id;
	
	END LOOP;  
    
    CLOSE cur1;
 
END$$
DELIMITER ;
				
CALL fix_exchange_flow_references;

-- mark all data stocks as modified to re-generate export ZIPs
UPDATE datastock_export_tag SET modified = 1;
