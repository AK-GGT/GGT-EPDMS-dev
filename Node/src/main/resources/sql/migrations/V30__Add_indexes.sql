-- TODO: we might want to drop indexes in case they're already existing
CREATE UNIQUE INDEX `idx_languages_LANGUAGECODE`
    ON `languages` (LANGUAGECODE)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE UNIQUE INDEX `idx_geographicalarea_AREACODE_areaName`
    ON `geographicalarea` (AREACODE, areaName)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE UNIQUE INDEX `idx_geographicalarea_AREACODE`
    ON `geographicalarea` (AREACODE)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE UNIQUE INDEX `idx_process_geography_ID_LOCATION`
    ON `process_geography` (ID, LOCATION)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_process_root_stock_id_visible_UUID`
    ON `process` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_process_root_stock_id_visible_VERSION_UUID`
    ON `process` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_process_root_stock_id_VERSION_UUID`
    ON `process` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE UNIQUE INDEX `idx_exchange_INTERNALID_EXCHANGEDIRECTION_ID`
    ON `exchange` (INTERNALID, EXCHANGEDIRECTION, ID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_lifecyclemodel_root_stock_id_visible_UUID`
    ON `lifecyclemodel` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_lifecyclemodel_root_stock_id_visible_VERSION_UUID`
    ON `lifecyclemodel` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_lifecyclemodel_root_stock_id_VERSION_UUID`
    ON `lifecyclemodel` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_lciamethod_root_stock_id_visible_UUID`
    ON `lciamethod` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_lciamethod_root_stock_id_visible_VERSION_UUID`
    ON `lciamethod` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_lciamethod_root_stock_id_VERSION_UUID`
    ON `lciamethod` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_flow_common_root_stock_id_visible_UUID`
    ON `flow_common` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_flow_common_root_stock_id_visible_VERSION_UUID`
    ON `flow_common` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_flow_common_root_stock_id_VERSION_UUID`
    ON `flow_common` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_flowproperty_root_stock_id_visible_UUID`
    ON `flowproperty` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_flowproperty_root_stock_id_visible_VERSION_UUID`
    ON `flowproperty` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_flowproperty_root_stock_id_VERSION_UUID`
    ON `flowproperty` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_unitgroup_root_stock_id_visible_UUID`
    ON `unitgroup` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_unitgroup_root_stock_id_visible_VERSION_UUID`
    ON `unitgroup` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_unitgroup_root_stock_id_VERSION_UUID`
    ON `unitgroup` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_source_root_stock_id_visible_UUID`
    ON `source` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_source_root_stock_id_visible_VERSION_UUID`
    ON `source` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_source_root_stock_id_VERSION_UUID`
    ON `source` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_contact_root_stock_id_visible_UUID`
    ON `contact` (root_stock_id, visible, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_contact_root_stock_id_visible_VERSION_UUID`
    ON `contact` (root_stock_id, visible, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE INDEX `idx_contact_root_stock_id_VERSION_UUID`
    ON `contact` (root_stock_id, VERSION, UUID)
    COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;
