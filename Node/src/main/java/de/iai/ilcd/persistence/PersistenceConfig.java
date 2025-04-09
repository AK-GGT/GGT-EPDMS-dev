package de.iai.ilcd.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

    @Bean(name = "soda4lcaEntityManagerFactory")
    public EntityManagerFactory getEntityManagerFactory() {
        // TODO: Let's use a data source managed by spring (see DBConfig.java)
        return Persistence.createEntityManagerFactory("soda4LCADBPU");
    }

}
