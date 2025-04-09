package de.iai.ilcd.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Persistence utility class
 */
public class PersistenceUtil implements ServletContextListener {

    /**
     * Logger
     */
    public static Logger logger = LoggerFactory.getLogger(de.iai.ilcd.persistence.PersistenceUtil.class);
    /**
     * The entity manager factory
     */
    private static EntityManagerFactory emf;

    /**
     * Get a new entity manager
     *
     * @return an entity manager
     */
    public static EntityManager getEntityManager() {

        if (emf == null) {
            throw new IllegalStateException("Context is not initialized yet.");
        }

        return emf.createEntityManager();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            logger.info("Initializing EntityManagerFactory");
            if (emf == null)
                emf = Persistence.createEntityManagerFactory("soda4LCADBPU");
            else
                logger.warn("EntityManagerFactory has been created before the servlet context has.");
        } catch (Throwable ex) {
            // We have to catch Throwable, otherwise we will miss
            // NoClassDefFoundError and other subclasses of Error
            logger.error("error initializing EntityManagerFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            if (emf.isOpen())
                emf.close();

        } catch (Throwable t) {
            logger.error("A problem occured when trying to close EntityManagerFactory.", t);
            if (emf.isOpen())
                emf.close();
        }
    }
}
