package eu.europa.ec.jrc.lca.commons.security.encryption;

import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public interface KeyLocation {
    boolean keyPairExists();

    boolean cipherKeyExists();

    RSAPublicKeySpec getPublicKey();

    RSAPrivateKeySpec getPrivateKey();

    byte[] getCipherKey();

    void store(RSAPublicKeySpec publicKey, RSAPrivateKeySpec privateKey);

    void storeCipherKey(byte[] cipherKey);
}
