package eu.europa.ec.jrc.lca.registry.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitServletContextListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(InitServletContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.info("starting Registry application under context path {}", event.getServletContext().getContextPath());
        logger.info(event.getServletContext().getServerInfo());

        ConfigurationService.migrateDatabaseSchema();
    }
}
