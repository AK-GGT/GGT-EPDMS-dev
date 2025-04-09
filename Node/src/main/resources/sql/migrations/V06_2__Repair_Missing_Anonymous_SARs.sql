-- for each datastock
--    if no SAR with ugid 0 exists
--       insert SAR with ugid 0
INSERT INTO `stock_access_right` (`stock_id`, `access_right_type`, `ug_id`, `value`) 
SELECT s.id, 'User', '0', '0' FROM `datastock` s WHERE s.id NOT IN (SELECT `stock_id` FROM `stock_access_right` WHERE `ug_id` = 0);
