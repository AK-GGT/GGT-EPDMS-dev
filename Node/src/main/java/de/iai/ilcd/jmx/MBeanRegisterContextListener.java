package de.iai.ilcd.jmx;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.configuration.InitServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;

public class MBeanRegisterContextListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(InitServletContextListener.class);
    private ApplicationInfo applicationInfo;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Registering Mbeans...");
        initializeApplicationInfoObject();
        try {
            registerAppInfoToMBeanServer();
        } catch (javax.management.InstanceAlreadyExistsException e) {
            logger.warn("JMX MBeans instance already exists");
        } catch (Exception e) {
            logger.error("JMX MBeans could not be initialized!", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private void initializeApplicationInfoObject() {
        ConfigurationService configurationService = ConfigurationService.INSTANCE;
        applicationInfo = new ApplicationInfo(configurationService);
    }

    private void registerAppInfoToMBeanServer() throws Exception {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("de.iai.ilcd.jmx:name=applicationInfo");
        platformMBeanServer.registerMBean(applicationInfo, objectName);
    }
}
