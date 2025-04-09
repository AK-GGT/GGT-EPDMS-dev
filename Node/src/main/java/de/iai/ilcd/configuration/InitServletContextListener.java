package de.iai.ilcd.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.jul.Log4jBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

public class InitServletContextListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(InitServletContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext context = event.getServletContext();
        Enumeration<String> params = context.getInitParameterNames();

        while (params.hasMoreElements()) {
            String param = (String) params.nextElement();
            String value = context.getInitParameter(param);
            if (param.startsWith("soda4LCA.systemproperty.")) {
                if (logger.isDebugEnabled())
                    logger.debug("Setting system property " + param.replace("soda4LCA.systemproperty.", "") + " to " + value);
                System.setProperty(param.replace("soda4LCA.systemproperty.", ""), value);
            }
        }

        ConfigurationService.INSTANCE.configureNodeInfo(event.getServletContext().getContextPath());

        Log4jBridgeHandler.install(false, "JUL", true);
    }
}
