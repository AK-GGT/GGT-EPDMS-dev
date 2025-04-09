ALTER TABLE `user` ADD `apikey` varchar(1071) DEFAULT NULL AFTER `PASSWORD_HASH_SALT`; 
ALTER TABLE `user` ADD `apikeyexpiry` varchar(255) DEFAULT NULL AFTER `apikey`;
ALTER TABLE `user` ADD `apikeyallowed` boolean DEFAULT true AFTER `apikeyexpiry`;