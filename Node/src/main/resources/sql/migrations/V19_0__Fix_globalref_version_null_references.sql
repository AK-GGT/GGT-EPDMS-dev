DROP PROCEDURE IF EXISTS fix_globalref_version_null_references;

-- this procedure repairs the version on globalreference objects that have 00.00.000 instead of null 
DELIMITER $$
CREATE PROCEDURE fix_globalref_version_null_references ()
   
BEGIN
	DECLARE globalref_id BIGINT(20);
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
			SELECT g.id, e.flow_id, e.flowreference_id, f.MAJORVERSION, f.MINORVERSION, f.SUBMINORVERSION, g.MAJORVERSION, g.MINORVERSION, g.SUBMINORVERSION  FROM exchange e 
				LEFT JOIN flow_common f ON f.ID = e.FLOW_ID
				LEFT JOIN globalreference g ON g.ID = e.FLOWREFERENCE_ID
				WHERE (f.MAJORVERSION IS NULL
					AND f.MINORVERSION IS NULL
					AND f.SUBMINORVERSION IS NULL
                    AND g.MAJORVERSION = 0
					AND g.MINORVERSION = 0
					AND g.SUBMINORVERSION = 0);
					
    DECLARE CONTINUE HANDLER
		FOR NOT FOUND SET done = TRUE;

    OPEN cur1;
    
    read_loop: LOOP
		FETCH cur1 INTO globalref_id, flow_id, flowreference_id, flow_version_maj, flow_version_min, flow_version_sub, flowreference_version_maj, flowreference_version_min, flowreference_version_sub;
		IF done THEN
		  LEAVE read_loop;
		END IF;

		-- set version to NULL
		UPDATE globalreference g
			SET g.MAJORVERSION = NULL, g.MINORVERSION = NULL, g.SUBMINORVERSION = NULL, g.VERSION = NULL
	        WHERE g.id = globalref_id;
	
	END LOOP;  
    
    CLOSE cur1;
 
END$$
DELIMITER ;
				
CALL fix_globalref_version_null_references;

-- mark all data stocks as modified to re-generate export ZIPs
UPDATE datastock_export_tag SET modified = 1;
