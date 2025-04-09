/**
 *
 */
package de.iai.ilcd.security.oidc;

import com.nimbusds.jose.JWSAlgorithm;
import de.iai.ilcd.configuration.ConfigurationService;
import org.pac4j.oidc.config.OidcConfiguration;

/**
 * @author MK
 * @since soda4LCA 6.7.3
 */
public class OIDCConfig extends OidcConfiguration {

    public OIDCConfig() {
//		String baseUri = "http://localhost:9090/auth", realm = "soda-relm";
//		this.setWithState(false);
//		this.setDiscoveryURI(baseUri + "/realms/" + realm + "/.well-known/openid-configuration");
//		this.setClientId("okworx");
//		this.setSecret("04c8d8a3-7f14-4489-9654-86993b50c01b");
//		this.setClientAuthenticationMethodAsString("client_secret_basic");
//		this.setPreferredJwsAlgorithm("RS256");
//		this.setScope("openid"); // default is openid+profile+email

        final var c = ConfigurationService.INSTANCE;

        this.setWithState(false);
//		this.setDiscoveryURI(baseUri + "/realms/" + realm + "/.well-known/openid-configuration");
        this.setDiscoveryURI(c.getOidcDiscoveryURI());
        this.setClientId(c.getOidcClientID());
        this.setSecret(c.getOidcSecret());
        this.setClientAuthenticationMethodAsString(c.getOidcClientAuthenticationMethod());
        this.setPreferredJwsAlgorithm(JWSAlgorithm.parse(c.getOidcPreferredJwsAlgorithm()));
        this.setScope(c.getOidcScope()); // default is openid+profile+email

    }

}
