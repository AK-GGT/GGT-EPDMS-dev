package eu.europa.ec.jrc.lca.commons.security;

import eu.europa.ec.jrc.lca.commons.security.encryption.KeyLength;
import eu.europa.ec.jrc.lca.commons.security.encryption.KeysGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.spec.RSAPublicKeySpec;


public class KeyGeneratorTest {

    private KeyPair kp = KeysGenerator.getKey(KeyLength._2048);

    @Test
    public void testGetKey() {
        KeyPair kp = KeysGenerator.getKey(KeyLength._2048);
        Assert.assertNotNull(kp);
    }

    @Test
    public void testGetPublicKey() {
        RSAPublicKeySpec publicKey = KeysGenerator.getPublicKey(kp);
        Assert.assertNotNull(publicKey);
    }

    @Test
    public void testGetPrivateKey() {
        RSAPublicKeySpec privateKey = KeysGenerator.getPublicKey(kp);
        Assert.assertNotNull(privateKey);
    }
}
