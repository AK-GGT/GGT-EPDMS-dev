package eu.europa.ec.jrc.lca.commons.security.encryption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;


@Component
public class FileKeyLocation implements KeyLocation {

    private static final Logger LOGGER = LogManager.getLogger(FileKeyLocation.class);
    private static final String PUBLIC_KEY_FILE_NAME = "public.key";
    private static final String PRIVATE_KEY_FILE_NAME = "private.key";
    private static final String CIPHER_KEY_FILE_NAME = "cipher.key";
    private String keyPath;

    public FileKeyLocation(@Value("${key.path}") String keyPath) {
        this.keyPath = keyPath;
    }

    @Override
    public boolean keyPairExists() {
        return new File(getLocation() + PUBLIC_KEY_FILE_NAME).exists()
                && new File(getLocation() + PRIVATE_KEY_FILE_NAME).exists();
    }

    @Override
    public boolean cipherKeyExists() {
        return new File(getLocation() + CIPHER_KEY_FILE_NAME).exists();
    }

    @Override
    public RSAPublicKeySpec getPublicKey() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("loading public key from " + getLocation() + PUBLIC_KEY_FILE_NAME);
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(getLocation() + PUBLIC_KEY_FILE_NAME)));

            BigInteger mod = (BigInteger) oin.readObject();
            BigInteger exp = (BigInteger) oin.readObject();
            return new RSAPublicKeySpec(mod, exp);
        } catch (FileNotFoundException e) {
            LOGGER.error("[getPublicKey]", e);
        } catch (IOException e) {
            LOGGER.error("[getPublicKey]", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("[getPublicKey]", e);
        } finally {
            try {
                oin.close();
            } catch (IOException | NullPointerException e) {
                LOGGER.error("[getPublicKey]", e);
            }
        }
        return null;
    }

    public RSAPublicKeySpec getPublicKey(String filePath) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("loading public key from " + filePath);
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(filePath)));

            BigInteger mod = (BigInteger) oin.readObject();
            BigInteger exp = (BigInteger) oin.readObject();
            return new RSAPublicKeySpec(mod, exp);
        } catch (FileNotFoundException e) {
            LOGGER.error("[getPublicKey]", e);
        } catch (IOException e) {
            LOGGER.error("[getPublicKey]", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("[getPublicKey]", e);
        } finally {
            try {
                oin.close();
            } catch (IOException | NullPointerException e) {
                LOGGER.error("[getPublicKey]", e);
            }
        }
        return null;
    }

    @Override
    public RSAPrivateKeySpec getPrivateKey() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("loading private key from " + getLocation() + PRIVATE_KEY_FILE_NAME);
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(getLocation() + PRIVATE_KEY_FILE_NAME)));

            BigInteger mod = (BigInteger) oin.readObject();
            BigInteger exp = (BigInteger) oin.readObject();
            return new RSAPrivateKeySpec(mod, exp);
        } catch (FileNotFoundException e) {
            LOGGER.error("[getPrivateKey]", e);
        } catch (IOException e) {
            LOGGER.error("[getPrivateKey]", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("[getPrivateKey]", e);
        } finally {
            try {
                oin.close();
            } catch (IOException | NullPointerException e) {
                LOGGER.error("[getPrivateKey]", e);
            }
        }
        return null;
    }

    public RSAPrivateKeySpec getPrivateKey(String filePath) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("loading private key from " + filePath);
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(filePath)));

            BigInteger mod = (BigInteger) oin.readObject();
            BigInteger exp = (BigInteger) oin.readObject();
            return new RSAPrivateKeySpec(mod, exp);
        } catch (FileNotFoundException e) {
            LOGGER.error("[getPrivateKey]", e);
        } catch (IOException e) {
            LOGGER.error("[getPrivateKey]", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("[getPrivateKey]", e);
        } finally {
            try {
                oin.close();
            } catch (IOException | NullPointerException e) {
                LOGGER.error("[getPrivateKey]", e);
            }
        }
        return null;
    }

    @Override
    public byte[] getCipherKey() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("loading cipher key from " + getLocation() + CIPHER_KEY_FILE_NAME);
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(getLocation() + CIPHER_KEY_FILE_NAME)));
            Object object = oin.readObject();

            byte[] data = (byte[]) object;

            return data;
        } catch (FileNotFoundException e) {
            LOGGER.error("[getCipherKey]", e);
        } catch (IOException e) {
            LOGGER.error("[getCipherKey]", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("[getCipherKey]", e);
        } finally {
            try {
                oin.close();
            } catch (IOException | NullPointerException e) {
                LOGGER.error("[getCipherKey]", e);
            }
        }
        return null;
    }

    @Override
    public void store(RSAPublicKeySpec publicKey, RSAPrivateKeySpec privateKey) {
        storePublic(publicKey);
        storePrivate(privateKey);
    }

    private void storePublic(RSAPublicKeySpec publicKey) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("storing public key at " + getLocation() + PUBLIC_KEY_FILE_NAME);
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(getLocation() + PUBLIC_KEY_FILE_NAME)));
            BigInteger mod = publicKey.getModulus();
            BigInteger exp = publicKey.getPublicExponent();
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (FileNotFoundException e) {
            LOGGER.error("[storePublic] " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("[storePublic] " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("[storePublic]", e);
        } finally {
            try {
                if (oout != null) {
                    oout.close();
                }
            } catch (IOException e) {
                LOGGER.error("[storePublic]", e);
            }
        }
    }

    private void storePrivate(RSAPrivateKeySpec privateKey) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("storing private key at " + getLocation() + PRIVATE_KEY_FILE_NAME);
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(getLocation() + PRIVATE_KEY_FILE_NAME)));
            BigInteger mod = privateKey.getModulus();
            BigInteger exp = privateKey.getPrivateExponent();
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (FileNotFoundException e) {
            LOGGER.error("[storePrivate] " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("[storePrivate] " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("[storePrivate]", e);
        } finally {
            try {
                if (oout != null) {
                    oout.close();
                }
            } catch (IOException e) {
                LOGGER.error("[storePrivate]", e);
            }
        }
    }

    @Override
    public void storeCipherKey(byte[] cipherKey) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("storing cipher key at " + getLocation() + CIPHER_KEY_FILE_NAME);
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(getLocation() + CIPHER_KEY_FILE_NAME)));
            oout.writeObject(cipherKey);
        } catch (FileNotFoundException e) {
            LOGGER.error("[storeCipher] " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("[storeCipher] " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("[storeCipher]", e);
        } finally {
            try {
                if (oout != null) {
                    oout.close();
                }
            } catch (IOException e) {
                LOGGER.error("[storeCipher]", e);
            }
        }
    }

    public String getLocation() {
        String path = System.getProperty("key.path", System.getProperty("catalina.base"));
        path = (path == null ? "" : path) + keyPath;
        LOGGER.debug("key path is " + path);
        return path;
    }
}
