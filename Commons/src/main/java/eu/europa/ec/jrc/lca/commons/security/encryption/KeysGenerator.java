package eu.europa.ec.jrc.lca.commons.security.encryption;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public final class KeysGenerator {
    private static final Logger LOGGER = LogManager.getLogger(KeysGenerator.class);

    private static final String ALGORITHM = "RSA";

    private KeysGenerator() {
    }

    public static KeyPair getKey(KeyLength length) {
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance(ALGORITHM);
            kpg.initialize(length.getSize());
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("[getKey]", e);
        }
        return null;
    }

    public static RSAPublicKeySpec getPublicKey(KeyPair kp) {
        try {
            return KeyFactory.getInstance(ALGORITHM).getKeySpec(kp.getPublic(),
                    RSAPublicKeySpec.class);
        } catch (InvalidKeySpecException e) {
            LOGGER.error("[getPublicKey]", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("[getPublicKey]", e);
        }
        return null;
    }

    /**
     * @param modulus_base64URL The "n" (modulus) parameter contains the modulus value for the
     *                          RSA public key. It is represented as a Base64urlUInt-encoded
     *                          value.
     * @param e_base64URL       The "e" (exponent) parameter contains the exponent value for
     *                          the RSA public key. It is represented as a
     *                          Base64urlUInt-encoded value.
     * @return
     */
    public static RSAPublicKeySpec getPublicKey(String modulus_base64URL, String e_base64URL) {
        var mod = new BigInteger(1, Base64.getUrlDecoder().decode(modulus_base64URL.trim().getBytes()));
        var e = new BigInteger(Base64.getUrlDecoder().decode(e_base64URL.trim().getBytes()));
        return new RSAPublicKeySpec(mod, e);
    }

    /**
     * @param pem without header, without footer and without separators
     * @return
     */
    public static RSAPublicKeySpec getPublicKey(String pem) {
        byte[] key = Base64.getDecoder().decode(pem);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            var x509 = new X509EncodedKeySpec(key);
            return keyFactory.getKeySpec(keyFactory.generatePublic(x509), RSAPublicKeySpec.class);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RSAPrivateKeySpec getPrivateKey(KeyPair kp) {
        try {
            return KeyFactory.getInstance(ALGORITHM).getKeySpec(kp.getPrivate(),
                    RSAPrivateKeySpec.class);
        } catch (InvalidKeySpecException e) {
            LOGGER.error("[getPrivateKey]", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("[getPrivateKey]", e);
        }
        return null;
    }

    /**
     * @param modulus_base64URL The "n" (modulus) parameter contains the modulus value for the
     *                          RSA public key. It is represented as a Base64urlUInt-encoded
     *                          value.
     * @param d_base64URL       The "d" (private exponent) parameter contains the private
     *                          exponent value for the RSA private key. It is represented as a
     *                          Base64urlUInt- encoded value.
     * @return
     */
    public static RSAPrivateKeySpec getPrivateKey(String modulus_base64URL, String d_base64URL) {
        var mod = new BigInteger(Base64.getUrlDecoder().decode(modulus_base64URL.trim().getBytes()));
        var d = new BigInteger(Base64.getUrlDecoder().decode(d_base64URL.trim().getBytes()));
        return new RSAPrivateKeySpec(mod, d);
    }

    public static byte[] getCipherKey(RSAPrivateKeySpec privateKey) {
        String exp = new String(privateKey.getPrivateExponent().toByteArray());
        String mod = new String(privateKey.getModulus().toByteArray());

        int iterations = 1000;

        char[] expChars = exp.toCharArray();
        char[] modChars = mod.toCharArray();

        byte[] salt;
        try {
            salt = getSalt();

            char[] chars = ArrayUtils.addAll(modChars, expChars);

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 128);
            SecretKeyFactory skf;
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash;
            hash = skf.generateSecret(spec).getEncoded();

            return hash;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("[getCipherKey]", e);
        } catch (InvalidKeySpecException e) {
            LOGGER.error("[getCipherKey]", e);
        }
        return null;
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        byte[] salt = new byte[16];

        random.nextBytes(salt);

        return salt;
    }
}
