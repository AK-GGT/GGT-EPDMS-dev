package eu.europa.ec.jrc.lca.commons.delegate;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;

public class ProxyEnabledRestServiceBD {

    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    public static final String HTTPS_PROXY_SYSPROP = "https_proxy";
    public static final String HTTP_PROXY_SYSPROP = "http_proxy";

    protected Client createClient(ClientConfig cc) {
        cc.connectorProvider(
                new HttpUrlConnectorProvider().connectionFactory(
                        new HttpUrlConnectorProvider.ConnectionFactory() {
                        Proxy p = null;
                        @Override
                        public HttpURLConnection getConnection(URL url)
                                throws IOException {
                            if (p == null) {
                                if (System.getenv().containsKey(HTTPS_PROXY_SYSPROP)) {
                                    logger.info("{} property detected with value {}", HTTPS_PROXY_SYSPROP, System.getenv(HTTPS_PROXY_SYSPROP));
                                    p = createProxy(System.getenv(HTTPS_PROXY_SYSPROP), 443);
                                } else if (System.getenv().containsKey(HTTP_PROXY_SYSPROP)) {
                                    logger.info("{} property detected with value {}", HTTP_PROXY_SYSPROP, System.getenv(HTTP_PROXY_SYSPROP));
                                    p = createProxy(System.getenv(HTTP_PROXY_SYSPROP), 80);
                                } else {
                                    p = Proxy.NO_PROXY;
                                    logger.debug("no proxy settings detected");
                                }
                            }
                            return (HttpURLConnection) url.openConnection(p);
                        }
                    })
        );

        if (logger.isDebugEnabled()) {
            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
            java.util.logging.Logger jerseyLogger = org.apache.logging.log4j.jul.LogManager.getLogManager().getLogger(this.getClass().getName());
            org.apache.logging.log4j.jul.LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
            cc.register(new LoggingFeature(jerseyLogger, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, LoggingFeature.DEFAULT_MAX_ENTITY_SIZE));
        }

        return ClientBuilder.newClient(cc);
    }

    protected Proxy createProxy(String proxyAddress, int defaultPort) {
        logger.debug("Setting proxy {}, default port is {}", proxyAddress, defaultPort);

        if (proxyAddress.startsWith("https://"))
            proxyAddress = proxyAddress.replaceAll("https://", "");
        else if (proxyAddress.startsWith("http://"))
            proxyAddress = proxyAddress.replaceAll("http://", "");

        String hostName = StringUtils.substringBefore(proxyAddress, ":");
        String portStr = StringUtils.substringAfter(proxyAddress, ":");

        if (logger.isDebugEnabled()) {
            logger.debug("proxy hostname: " + hostName);
            logger.debug("proxy port: " + portStr);
        }

        int port = (StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : defaultPort);
        logger.info("Using proxy {} at port {}", hostName, port);

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
    }

}
