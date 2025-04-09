DROP PROCEDURE IF EXISTS PROC_DROP_INDEX;

DELIMITER #

CREATE PROCEDURE PROC_DROP_INDEX(IN tableName VARCHAR(64), IN constraintName VARCHAR(64))
BEGIN
    IF EXISTS(
        SELECT * FROM information_schema.table_constraints
        WHERE 
            table_schema    = DATABASE()     AND
            table_name      = tableName      AND
            constraint_name = constraintName AND
            constraint_type = 'INDEX')
    THEN
        SET @query = CONCAT('ALTER TABLE ', tableName, ' DROP INDEX ', constraintName, ';');
        PREPARE stmt FROM @query; 
        EXECUTE stmt; 
        DEALLOCATE PREPARE stmt; 
    END IF; 
END #

DELIMITER ;

CALL PROC_DROP_INDEX('networknode', 'BASEURL');
CALL PROC_DROP_INDEX('networknode', 'NAME');
CALL PROC_DROP_INDEX('networknode', 'NODEID');
