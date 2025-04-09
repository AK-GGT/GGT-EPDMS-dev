ALTER TABLE `lciamethod` ADD referencequantity_ID BIGINT DEFAULT NULL;

ALTER TABLE `lciamethod` ADD CONSTRAINT FK_lciamethod_referencequantity_ID FOREIGN KEY (referencequantity_ID) REFERENCES globalreference (ID) ON DELETE CASCADE;
