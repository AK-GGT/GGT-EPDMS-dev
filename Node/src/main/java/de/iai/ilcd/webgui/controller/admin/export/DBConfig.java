package de.iai.ilcd.webgui.controller.admin.export;

import de.iai.ilcd.configuration.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

@Configuration
public class DBConfig {

    @Bean
    public static DataSource getDataSource() {

        // expected to be jdbc/soda4LCAdbconnection
        String ds = ConfigurationService.INSTANCE.getAppConfig()
                .getString("persistence.dbConnection", "jdbc/soda4LCAdbconnection");
        return new JndiDataSourceLookup().getDataSource(ds);
    }

    @Bean
    public static JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(getDataSource());
    }

    @Bean
    public static NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(getDataSource());
    }
}
