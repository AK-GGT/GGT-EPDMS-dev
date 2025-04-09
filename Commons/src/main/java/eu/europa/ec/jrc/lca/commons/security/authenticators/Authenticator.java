package eu.europa.ec.jrc.lca.commons.security.authenticators;

import javax.ws.rs.container.ContainerRequestContext;

public interface Authenticator {
    void authenticate(ContainerRequestContext request);
}
