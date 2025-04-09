package de.iai.ilcd.security.oidc;

import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.shiro.util.Factory;

/**
 * @author MK
 * @since soda4LCA 6.7.3
 */
public class CallbackFilterDefaultUrl implements Factory<String> {

    @Override
    public String getInstance() {
//		return "http://localhost:8080/Node/";
        return ConfigurationService.INSTANCE.getOidcCallbackFilterDefaultUrl();
    }

}
