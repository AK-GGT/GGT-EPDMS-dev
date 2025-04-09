package eu.europa.ec.jrc.lca.commons.security.filter;

import eu.europa.ec.jrc.lca.commons.security.annotations.Secured;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.*;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;

@Component
public class SecuredResourceFilterFactory implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(Secured.class)) {
            featureContext.register(new SecurityResourceFilter());
        }
    }

    private class SecurityResourceFilter implements ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) {
            ApplicationContextHolder.getApplicationContext().getBean(SecurityFilter.class).filter(requestContext);
        }

        @Override
        public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {

        }
    }
}

