package edu.kit.soda4lca.test.ui.admin.t151ExportCacheTest;

import de.iai.ilcd.model.datastock.ExportType;
import de.iai.ilcd.webgui.controller.util.ExportMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

final class Stock {

    private static final Logger log = LogManager.getLogger(Stock.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final JdbcTemplate jdbcTemplate;
    private final String name;
    private final long id;
    private final String uuid;

    private final boolean isRoot;

    private Stock(@Nonnull String stockName, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.name = Objects.requireNonNull(stockName);
        final var q = String.format(
                "SELECT `ID` as `id`, `UUID` as `uuid`, `datastock_type` as `type`" +
                        " FROM `datastock`" +
                        " WHERE `name` = '%s'",
                stockName
        );
        Map<String, Object> resultMap = jdbcTemplate.queryForMap(q);
        this.id = (long) resultMap.get("id");
        this.uuid = (String) resultMap.get("uuid");
        this.isRoot = "rds".equals(resultMap.get("type"));
    }

    static Stock initFromDB(@Nonnull String stockName, JdbcTemplate jdbcTemplate) {
        return new Stock(stockName, jdbcTemplate);
    }

    void flag(ExportCacheFlag flag) {
        var q = String.format(
                "UPDATE `datastock_export_tag`" +
                        " SET `modified` = %d" +
                        " WHERE `datastock_id` = %d",
                flag.getValue(), this.id
        );
        jdbcTemplate.update(q);
    }

    boolean isModified() {
        return isCompletelyModified(true);
    }

    boolean isUnmodified() {
        return isCompletelyModified(false);
    }

    boolean isCompletelyModified(boolean modified) {
        log.trace("Checking whether stock is completely {} ...", (modified ? "modified" : "unmodified"));
        log.trace("Stock: {}", this);

        final var q = String.format(
                "SELECT count(`ID`)" +
                        " FROM `datastock_export_tag`" +
                        " WHERE `datastock_id` = %d",
                this.id
        );
        log.trace("All count query: '{}'", q);

        final long exportTagCount = Objects.requireNonNull(jdbcTemplate.queryForObject(q, Long.class));
        log.trace("Result: {}", exportTagCount);

        final var q2 = q + " AND `modified` = " + (modified ? 1 : 0);
        log.trace("{} count query: '{}'", (modified ? "Modified" : "Un-modified"), q2);

        final long goodTagsCount = Objects.requireNonNull(jdbcTemplate.queryForObject(q2, Long.class));
        log.trace("Result: {}", goodTagsCount);

        return exportTagCount == goodTagsCount;
    }

    boolean isModified(@Nonnull final ExportMode exportMode,
                       final ExportType... exportTypes) {
        return isCompletelyModified(true, exportMode, exportTypes);
    }

    boolean isUnmodified(@Nonnull final ExportMode exportMode,
                         final ExportType... exportTypes) {
        return isCompletelyModified(false, exportMode, exportTypes);
    }

    boolean isCompletelyModified(boolean modified,
                                 @Nonnull final ExportMode exportMode,
                                 final ExportType... exportTypes) {
        log.trace("Checking whether stock is flagged {} ...", (modified ? "modified" : "unmodified"));
        log.trace("Stock: {}", this);
        log.trace("ExportMode: {}", exportMode); // export types will be traced a few lines down

        final var tmp = (exportTypes == null || exportTypes.length == 0) ? null :
                Arrays.stream(exportTypes)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        final var exportTypeList = (tmp == null || tmp.size() == 0) ? null : tmp;
        log.trace("ExportTypes: {}", exportTypeList);

        var q = String.format(
                "SELECT count(`ID`)" +
                        " FROM `datastock_export_tag`" +
                        " WHERE `datastock_id` = %d" +
                        " AND `mode` = %d",
                this.id, exportMode.ordinal()
        );
        if (exportTypeList != null) {
            final var typeClauses = exportTypeList.stream()
                    .filter(Objects::nonNull)
                    .map(t -> String.format("`type` = %d", t.ordinal()))
                    .collect(Collectors.toList());
            q = q + String.format(" AND (%s)", String.join(" OR ", typeClauses));
        }
        final var qAllCount = q;
        log.trace("All count query: '{}'", qAllCount);

        final long allCount = Objects.requireNonNull(jdbcTemplate.queryForObject(qAllCount, Long.class));
        log.trace("Result: {}", allCount);

        final var qGoodCount = qAllCount + " AND `modified` = " + (modified ? 1 : 0);
        log.trace("{} count query: '{}'", (modified ? "Modified" : "Unmodified"), qGoodCount);

        final long goodCount = Objects.requireNonNull(jdbcTemplate.queryForObject(qGoodCount, Long.class));
        log.trace("Result: {}", goodCount);

        return allCount == goodCount;
    }

    boolean hasTag(ExportMode exportMode, ExportType exportType) {
        try {
            final var q = String.format(
                    "SELECT `id` FROM `datastock_export_tag`" +
                            " WHERE `datastock_id` = %d" +
                            " AND `type` = %d" +
                            " AND `mode` = %d",
                    this.id, exportType.ordinal(), exportMode.ordinal()
            );
            log.trace("Looking up export tag (type: '{}') for data stock '{}', using '{}'", exportType.name(), this.name, q);
            final var id = jdbcTemplate.queryForObject(q, String.class);
            log.trace("Id: '{}'", id);

            return id != null;

        } catch (EmptyResultDataAccessException e) {
            log.trace("No export tag (mode: '{}', type'{}') found for {}", exportMode.name(), exportType.name(), this, e);
        } catch (Exception e) {
            log.error("Error occurred while checking whether export tag exists  {}Mode: '{}' ({}) {}Type: '{}' ({}) {}Stock: {}", LINE_SEPARATOR, exportMode.name(), exportMode.ordinal(), LINE_SEPARATOR, exportType.name(), exportType.ordinal(), LINE_SEPARATOR, this, e);
        }

        return false;
    }

    Path getCurrentExportCacheFilePath(ExportMode exportMode, ExportType cacheType) {
        try {
            String q = String.format(
                    "SELECT `file` FROM `datastock_export_tag`" +
                            " WHERE `datastock_id` = %d" +
                            " AND `type` = %d" +
                            " AND `mode` = %d",
                    this.id, cacheType.ordinal(), exportMode.ordinal()
            );
            log.trace("Looking up cache file name (type: '{}') for data stock '{}', using '{}'", cacheType.name(), this.name, q);
            final var fileName = jdbcTemplate.queryForObject(q, String.class);
            log.trace("File name: '{}'", fileName);
            return Paths.get(Objects.requireNonNull(fileName));
        } catch (Exception e) {
            if (log.isTraceEnabled())
                log.warn(String.format("Failed to determine cache file name for stock '%s'", this.name), e);
            return null; // It's not guaranteed to exist, so let's just return null.
        }
    }

    boolean contains(UUID datasetUuid) {
        String q;
        if (isRoot) {
            q = String.format(
                    "SELECT count(*)" + // it should be 1 or 0 due to where clause
                            " FROM `process`" +
                            " WHERE `UUID` = '%s'" +
                            "     AND `root_stock_id` = %d",
                    datasetUuid, this.id
            );
        } else { // logical stock
            q = String.format(
                    "SELECT count(*)" + // it should be 1 or 0 due to where clause
                            " FROM `process`" +
                            "    JOIN `datastock_process` AS `xref` ON `process`.`ID` = `xref`.`processes_ID`" +
                            "    JOIN `datastock` AS `logical_stock` ON `xref`.`containingDataStocks_ID` = `logical_stock`.`ID`" +
                            " WHERE `process`.`UUID` = '%s'" +
                            "     AND `logical_stock`.`ID` = %d",
                    datasetUuid, this.id
            );
        }
        return Long.valueOf(1).equals(jdbcTemplate.queryForObject(q, Long.class));
    }

    @Override
    public String toString() {
        return String.format("%s{name: '%s', id: %d, uuid: '%s'}", this.getClass().getSimpleName(), name, id, uuid);
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }
}
