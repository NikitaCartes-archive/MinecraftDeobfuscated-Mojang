/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crypt {
    private static final Logger LOGGER = LogManager.getLogger();

    @Environment(value=EnvType.CLIENT)
    public static SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new Error(noSuchAlgorithmException);
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
            LOGGER.error("Key pair generation failed!");
            return null;
        }
    }

    public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) {
        try {
            return Crypt.digestData("SHA-1", string.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
            return null;
        }
    }

    private static byte[] digestData(String string, byte[] ... bs) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(string);
            for (byte[] cs : bs) {
                messageDigest.update(cs);
            }
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
            return null;
        }
    }

    public static PublicKey byteToPublicKey(byte[] bs) {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bs);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
        } catch (InvalidKeySpecException invalidKeySpecException) {
            // empty catch block
        }
        LOGGER.error("Public key reconstitute failed!");
        return null;
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bs) {
        return new SecretKeySpec(Crypt.decryptUsingKey(privateKey, bs), "AES");
    }

    @Environment(value=EnvType.CLIENT)
    public static byte[] encryptUsingKey(Key key, byte[] bs) {
        return Crypt.cipherData(1, key, bs);
    }

    public static byte[] decryptUsingKey(Key key, byte[] bs) {
        return Crypt.cipherData(2, key, bs);
    }

    private static byte[] cipherData(int i, Key key, byte[] bs) {
        try {
            return Crypt.setupCipher(i, key.getAlgorithm(), key).doFinal(bs);
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            illegalBlockSizeException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        }
        LOGGER.error("Cipher data failed!");
        return null;
    }

    private static Cipher setupCipher(int i, String string, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(string);
            cipher.init(i, key);
            return cipher;
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        }
        LOGGER.error("Cipher creation failed!");
        return null;
    }

    public static Cipher getCipher(int i, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(i, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        } catch (GeneralSecurityException generalSecurityException) {
            throw new RuntimeException(generalSecurityException);
        }
    }
}

