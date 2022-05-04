/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CryptException;

public class Crypt {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_BITS = 128;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_BITS = 1024;
    private static final String BYTE_ENCODING = "ISO_8859_1";
    private static final String HASH_ALGORITHM = "SHA-1";
    public static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
    public static final String RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----";
    private static final String RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";
    public static final String MIME_LINE_SEPARATOR = "\r\n";
    public static final Codec<PublicKey> PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(Crypt.stringToRsaPublicKey(string));
        } catch (CryptException cryptException) {
            return DataResult.error(cryptException.getMessage());
        }
    }, Crypt::rsaPublicKeyToString);
    public static final Codec<PrivateKey> PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(Crypt.stringToPemRsaPrivateKey(string));
        } catch (CryptException cryptException) {
            return DataResult.error(cryptException.getMessage());
        }
    }, Crypt::pemRsaPrivateKeyToString);

    public static SecretKey generateSecretKey() throws CryptException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static KeyPair generateKeyPair() throws CryptException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) throws CryptException {
        try {
            return Crypt.digestData(string.getBytes(BYTE_ENCODING), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static byte[] digestData(byte[] ... bs) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        for (byte[] cs : bs) {
            messageDigest.update(cs);
        }
        return messageDigest.digest();
    }

    private static <T extends Key> T rsaStringToKey(String string, String string2, String string3, ByteArrayToKeyFunction<T> byteArrayToKeyFunction) throws CryptException {
        int i = string.indexOf(string2);
        if (i != -1) {
            int j = string.indexOf(string3, i += string2.length());
            string = string.substring(i, j + 1);
        }
        return byteArrayToKeyFunction.apply(Base64.getMimeDecoder().decode(string));
    }

    public static PrivateKey stringToPemRsaPrivateKey(String string) throws CryptException {
        return Crypt.rsaStringToKey(string, PEM_RSA_PRIVATE_KEY_HEADER, PEM_RSA_PRIVATE_KEY_FOOTER, Crypt::byteToPrivateKey);
    }

    public static PublicKey stringToRsaPublicKey(String string) throws CryptException {
        return Crypt.rsaStringToKey(string, RSA_PUBLIC_KEY_HEADER, RSA_PUBLIC_KEY_FOOTER, Crypt::byteToPublicKey);
    }

    public static String rsaPublicKeyToString(PublicKey publicKey) {
        if (!ASYMMETRIC_ALGORITHM.equals(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException("Public key must be RSA");
        }
        return "-----BEGIN RSA PUBLIC KEY-----\r\n" + Base64.getMimeEncoder().encodeToString(publicKey.getEncoded()) + "\r\n-----END RSA PUBLIC KEY-----\r\n";
    }

    public static String pemRsaPrivateKeyToString(PrivateKey privateKey) {
        if (!ASYMMETRIC_ALGORITHM.equals(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException("Private key must be RSA");
        }
        return "-----BEGIN RSA PRIVATE KEY-----\r\n" + Base64.getMimeEncoder().encodeToString(privateKey.getEncoded()) + "\r\n-----END RSA PRIVATE KEY-----\r\n";
    }

    private static PrivateKey byteToPrivateKey(byte[] bs) throws CryptException {
        try {
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bs);
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
            return keyFactory.generatePrivate(encodedKeySpec);
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static PublicKey byteToPublicKey(byte[] bs) throws CryptException {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bs);
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bs) throws CryptException {
        byte[] cs = Crypt.decryptUsingKey(privateKey, bs);
        try {
            return new SecretKeySpec(cs, SYMMETRIC_ALGORITHM);
        } catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

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

    static interface ByteArrayToKeyFunction<T extends Key> {
        public T apply(byte[] var1) throws CryptException;
    }

    public record SaltSignaturePair(long salt, byte[] signature) {
        public static final SaltSignaturePair EMPTY = new SaltSignaturePair(0L, ByteArrays.EMPTY_ARRAY);

        public SaltSignaturePair(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readLong(), friendlyByteBuf.readByteArray());
        }

        public boolean isValid() {
            return this.signature.length > 0;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeLong(this.salt);
            friendlyByteBuf.writeByteArray(this.signature);
        }

        public byte[] saltAsBytes() {
            return Longs.toByteArray(this.salt);
        }
    }

    public static class SaltSupplier {
        private static final SecureRandom secureRandom = new SecureRandom();

        public static long getLong() {
            return secureRandom.nextLong();
        }
    }
}

