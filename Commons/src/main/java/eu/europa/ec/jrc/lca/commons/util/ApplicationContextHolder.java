package eu.europa.ec.jrc.lca.commons.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {
    private static ApplicationContext ctx;

    public ApplicationContextHolder() {
    }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

}