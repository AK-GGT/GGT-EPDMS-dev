package eu.europa.ec.jrc.lca.registry.db.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Randomize initial default data stock UUID
 */
public class V1_1__DataStockUUID implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V1_1__DataStockUUID.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        V1_1__DataStockUUID.LOGGER.info("Randomize initial default data stock UUID.");

        final String sqlSetUUID = "UPDATE `datastock` SET `UUID`=? WHERE `ID`=?";

        // set random UUID for default data stock
        BatchPreparedStatementSetter pstSet = new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setLong(2, 1L);
            }

            @Override
            public int getBatchSize() {
                return 1;
            }
        };
        jdbcTemplate.batchUpdate(sqlSetUUID, pstSet);
    }

}
