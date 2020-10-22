/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.CryptException;

public class Crypt {
    @Environment(value=EnvType.CLIENT)
    public static SecretKey generateSecretKey() throws CryptException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static KeyPair generateKeyPair() throws CryptException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) throws CryptException {
        try {
            return Crypt.digestData(string.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static byte[] digestData(byte[] ... bs) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        for (byte[] cs : bs) {
            messageDigest.update(cs);
        }
        return messageDigest.digest();
    }

    @Environment(value=EnvType.CLIENT)
    public static PublicKey byteToPublicKey(byte[] bs) throws CryptException {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bs);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bs) throws CryptException {
        byte[] cs = Crypt.decryptUsingKey(privateKey, bs);
        try {
            return new SecretKeySpec(cs, "AES");
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static byte[] encryptUsingKey(Key key, byte[] bs) throws CryptException {
        return Crypt.cipherData(1, key, bs);
    }

    public static byte[] decryptUsingKey(Key key, byte[] bs) throws CryptException {
        return Crypt.cipherData(2, key, bs);
    }

    private static byte[] cipherData(int i, Key key, byte[] bs) throws CryptException {
        try {
            return Crypt.setupCipher(i, key.getAlgorithm(), key).doFinal(bs);
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static Cipher setupCipher(int i, String string, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(string);
        cipher.init(i, key);
        return cipher;
    }

    public static Cipher getCipher(int i, Key key) throws CryptException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(i, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }
}

