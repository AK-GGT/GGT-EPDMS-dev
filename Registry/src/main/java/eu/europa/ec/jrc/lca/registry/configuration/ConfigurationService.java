package eu.europa.ec.jrc.lca.registry.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConfigurationService {

    private static final Logger logger = LogManager.getLogger(ConfigurationService.class);

    public static void migrateDatabaseSchema() {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("app.properties");
            String dbconn = config.getString("persistence.dbConnection");

            Context ctx = new InitialContext();

            DataSource dataSource = (DataSource) ctx.lookup(dbconn);

            Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource);
            flyway.setLocations("sql/migrations");

            ConfigurationService.logSchemaStatus(flyway);

            flyway.setValidateOnMigrate(true);

            int migrations = flyway.migrate();

            if (migrations > 0) {
                ConfigurationService.logger.info("database schema: successfully migrated");
                ConfigurationService.logSchemaStatus(flyway);
            }

        } catch (FlywayException e) {
            ConfigurationService.logger.error("error migrating database schema", e);
            throw new RuntimeException("FATAL ERROR: database schema is not properly initialized", e);
        } catch (NamingException e) {
            ConfigurationService.logger.error("error looking up datasource", e);
            throw new RuntimeException("FATAL ERROR: could not lookup datasource", e);
        } catch (ConfigurationException e) {
            ConfigurationService.logger.error("error reading app.properties", e);
            throw new RuntimeException("FATAL ERROR: could not read app.properties", e);
        }

    }

    private static void logSchemaStatus(Flyway flyway) {
        if (flyway.info().current() != null) {
            ConfigurationService.logger.info("database schema: current version is " + flyway.info().current().getVersion());
        } else {
            ConfigurationService.logger.info("database schema: no migration has been applied yet.");
        }
    }

}
