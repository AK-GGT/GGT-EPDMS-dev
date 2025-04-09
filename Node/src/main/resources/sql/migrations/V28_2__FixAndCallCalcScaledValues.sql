-- this will prevent the "The user specified as a definer ('someusername'@'%') does not exist" error
-- when importing a dump from another machine
ALTER PROCEDURE calculate_scaled_values_for_batch_qa SQL SECURITY INVOKER;

CALL calculate_scaled_values_for_batch_qa;
