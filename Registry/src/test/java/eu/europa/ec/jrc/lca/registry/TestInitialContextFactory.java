package eu.europa.ec.jrc.lca.registry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInitialContextFactory implements InitialContextFactory {

    private final Context mockContext = mock(InitialContext.class);

    public Context getInitialContext(Hashtable<?, ?> arg0)
            throws NamingException {
        when(mockContext.lookup("java:comp/env")).thenReturn(mockContext);
        when(mockContext.lookup("registry.mail.smtp.host")).thenReturn("localhost");
        when(mockContext.lookup("registry.mail.smtp.port")).thenReturn(25);
        when(mockContext.lookup("registry.mail.smtp.auth")).thenReturn(false);
        when(mockContext.lookup("registry.mail.smtp.from")).thenReturn("registry@local");
        return mockContext;
    }
}

