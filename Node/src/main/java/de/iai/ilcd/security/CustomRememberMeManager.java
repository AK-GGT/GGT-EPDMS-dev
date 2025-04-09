package de.iai.ilcd.security;

import eu.europa.ec.jrc.lca.commons.security.SecurityContext;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

public class CustomRememberMeManager extends CookieRememberMeManager {

    public CustomRememberMeManager() {
        super();
        SecurityContext securityContext = ApplicationContextHolder.getApplicationContext().getBean("securityContext", SecurityContext.class);
        byte[] cipherKey = securityContext.getCipherKey();
        setCipherKey(cipherKey);
    }

}
