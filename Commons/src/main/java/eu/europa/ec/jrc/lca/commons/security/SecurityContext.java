package eu.europa.ec.jrc.lca.commons.security;

import eu.europa.ec.jrc.lca.commons.security.encryption.KeyLength;
import eu.europa.ec.jrc.lca.commons.security.encryption.KeyLocation;
import eu.europa.ec.jrc.lca.commons.security.encryption.KeysGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

@Component
public class SecurityContext {

    private static final Logger LOGGER = LogManager.getLogger(SecurityContext.class);

    @Autowired
    private KeyLocation keyLocation;

    @Value("${key.length}")
    private int length;

    private RSAPublicKeySpec publicKey;

    private RSAPrivateKeySpec privateKey;

    private byte[] cipherKey;

    @PostConstruct
    private void loadOrCreateKey() {
        if (keyLocation.keyPairExists()) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("using existing key pair");
            publicKey = keyLocation.getPublicKey();
            privateKey = keyLocation.getPrivateKey();
        } else {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("generating new key pair");
            KeyPair kp = KeysGenerator.getKey(KeyLength.get(length));
            publicKey = KeysGenerator.getPublicKey(kp);
            privateKey = KeysGenerator.getPrivateKey(kp);
            keyLocation.store(publicKey, privateKey);
        }

        if (keyLocation.cipherKeyExists()) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("using existing cipher key");
            cipherKey = keyLocation.getCipherKey();
        } else {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("generating new cipher key");
            cipherKey = KeysGenerator.getCipherKey(privateKey);
            keyLocation.storeCipherKey(cipherKey);
        }
    }

    public RSAPublicKeySpec getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKeySpec getPrivateKey() {
        return privateKey;
    }

    public byte[] getCipherKey() {
        return cipherKey;
    }

}
