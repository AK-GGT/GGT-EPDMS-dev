package de.iai.ilcd.db.migrations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Initialize datastock display properties with ascending ordinal values to preserve original order
 */
public class V2_7__DataStockDisplayProps implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V2_7__DataStockDisplayProps.class);

    private int cnt = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        V2_7__DataStockDisplayProps.LOGGER.info("Initialize datastock display properties");

        final String sqlInsertDisplayProps = "INSERT INTO `datastock_display_props` (`hidden`, `ordinal`) VALUES (?, ?)";

        BatchPreparedStatementSetter pstInsertDP = new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, 0);
                ps.setInt(2, V2_7__DataStockDisplayProps.this.cnt);
            }

            @Override
            public int getBatchSize() {
                return 1;
            }
        };

        int datastocksCount = jdbcTemplate.queryForObject("SELECT count(*) FROM `datastock`", int.class);

        for (cnt = 0; cnt < datastocksCount; cnt++) {
            jdbcTemplate.batchUpdate(sqlInsertDisplayProps, pstInsertDP);
            long displayPropsId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", long.class);
            long datastockId = jdbcTemplate.queryForObject("SELECT `ID` FROM `datastock` LIMIT 1 OFFSET " + cnt, long.class);
            jdbcTemplate.update("UPDATE `datastock` SET `display_props_id`= " + displayPropsId + " WHERE `ID`=" + datastockId);
        }

    }

}
