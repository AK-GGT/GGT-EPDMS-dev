package eu.europa.ec.jrc.lca.commons.security.filter;

import eu.europa.ec.jrc.lca.commons.security.authenticators.Authenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class SecurityFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LogManager.getLogger(SecurityFilter.class);

    @Autowired
    private Authenticator authenticator;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ByteArrayOutputStream baos = getClonedInputStream(requestContext.getEntityStream());
        requestContext.setEntityStream(new ByteArrayInputStream(baos.toByteArray()));
        authenticator.authenticate(requestContext);
        requestContext.setEntityStream(new ByteArrayInputStream(baos.toByteArray()));
    }


    private ByteArrayOutputStream getClonedInputStream(InputStream str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = str.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            LOGGER.error("[getClonedInputStream]", e);
        } finally {
            try {
                str.close();
            } catch (IOException e) {
                LOGGER.error("[getClonedInputStream]", e);
            }
        }
        return baos;
    }
}
