package de.iai.ilcd.security;

import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The filter for checking whether REST service has token lock.
 *
 * @author sarai
 */
public class AuthenticationAccessFilter extends AnonymousFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (ConfigurationService.INSTANCE.isTokenOnly()) {
            response.resetBuffer();
            try {
                response.getOutputStream().write("Permission denied.".getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

}
