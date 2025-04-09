ALTER TABLE `user` ADD `resetkey` varchar(255) DEFAULT NULL AFTER `REGISTRATIONKEY`;
ALTER TABLE `user` ADD `resettimestamp` varchar(255) DEFAULT NULL AFTER `resetkey`;