package net.minecraft.util;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;

public class Crypt {
	private static final String SYMMETRIC_ALGORITHM = "AES";
	private static final int SYMMETRIC_BITS = 128;
	private static final String ASYMMETRIC_ALGORITHM = "RSA";
	private static final int ASYMMETRIC_BITS = 1024;
	private static final String BYTE_ENCODING = "ISO_8859_1";
	private static final String HASH_ALGORITHM = "SHA-1";
	private static final String PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
	private static final String PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
	public static final String RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----";
	private static final String RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";
	public static final String MIME_LINE_SEPARATOR = "\r\n";
	public static final Codec<PublicKey> PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap(string -> {
		try {
			return DataResult.success(stringToRsaPublicKey(string));
		} catch (CryptException var2) {
			return DataResult.error(var2.getMessage());
		}
	}, Crypt::rsaPublicKeyToString);
	public static final Codec<PrivateKey> PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap(string -> {
		try {
			return DataResult.success(stringToPemRsaPrivateKey(string));
		} catch (CryptException var2) {
			return DataResult.error(var2.getMessage());
		}
	}, Crypt::pemRsaPrivateKeyToString);

	public static SecretKey generateSecretKey() throws CryptException {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			return keyGenerator.generateKey();
		} catch (Exception var1) {
			throw new CryptException(var1);
		}
	}

	public static KeyPair generateKeyPair() throws CryptException {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);
			return keyPairGenerator.generateKeyPair();
		} catch (Exception var1) {
			throw new CryptException(var1);
		}
	}

	public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) throws CryptException {
		try {
			return digestData(string.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
		} catch (Exception var4) {
			throw new CryptException(var4);
		}
	}

	private static byte[] digestData(byte[]... bs) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

		for (byte[] cs : bs) {
			messageDigest.update(cs);
		}

		return messageDigest.digest();
	}

	private static <T extends Key> T rsaStringToKey(String string, String string2, String string3, Crypt.ByteArrayToKeyFunction<T> byteArrayToKeyFunction) throws CryptException {
		int i = string.indexOf(string2);
		if (i != -1) {
			i += string2.length();
			int j = string.indexOf(string3, i);
			string = string.substring(i, j + 1);
		}

		return byteArrayToKeyFunction.apply(Base64.getMimeDecoder().decode(string));
	}

	public static PrivateKey stringToPemRsaPrivateKey(String string) throws CryptException {
		return rsaStringToKey(string, "-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----", Crypt::byteToPrivateKey);
	}

	public static PublicKey stringToRsaPublicKey(String string) throws CryptException {
		return rsaStringToKey(string, "-----BEGIN RSA PUBLIC KEY-----", "-----END RSA PUBLIC KEY-----", Crypt::byteToPublicKey);
	}

	public static String rsaPublicKeyToString(PublicKey publicKey) {
		if (!"RSA".equals(publicKey.getAlgorithm())) {
			throw new IllegalArgumentException("Public key must be RSA");
		} else {
			return "-----BEGIN RSA PUBLIC KEY-----\r\n" + Base64.getMimeEncoder().encodeToString(publicKey.getEncoded()) + "\r\n-----END RSA PUBLIC KEY-----\r\n";
		}
	}

	public static String pemRsaPrivateKeyToString(PrivateKey privateKey) {
		if (!"RSA".equals(privateKey.getAlgorithm())) {
			throw new IllegalArgumentException("Private key must be RSA");
		} else {
			return "-----BEGIN RSA PRIVATE KEY-----\r\n" + Base64.getMimeEncoder().encodeToString(privateKey.getEncoded()) + "\r\n-----END RSA PRIVATE KEY-----\r\n";
		}
	}

	private static PrivateKey byteToPrivateKey(byte[] bs) throws CryptException {
		try {
			EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bs);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePrivate(encodedKeySpec);
		} catch (Exception var3) {
			throw new CryptException(var3);
		}
	}

	public static PublicKey byteToPublicKey(byte[] bs) throws CryptException {
		try {
			EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bs);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(encodedKeySpec);
		} catch (Exception var3) {
			throw new CryptException(var3);
		}
	}

	public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bs) throws CryptException {
		byte[] cs = decryptUsingKey(privateKey, bs);

		try {
			return new SecretKeySpec(cs, "AES");
		} catch (Exception var4) {
			throw new CryptException(var4);
		}
	}

	public static byte[] encryptUsingKey(Key key, byte[] bs) throws CryptException {
		return cipherData(1, key, bs);
	}

	public static byte[] decryptUsingKey(Key key, byte[] bs) throws CryptException {
		return cipherData(2, key, bs);
	}

	private static byte[] cipherData(int i, Key key, byte[] bs) throws CryptException {
		try {
			return setupCipher(i, key.getAlgorithm(), key).doFinal(bs);
		} catch (Exception var4) {
			throw new CryptException(var4);
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
		} catch (Exception var3) {
			throw new CryptException(var3);
		}
	}

	public static void updateChatSignature(Signature signature, long l, UUID uUID, Instant instant, String string) throws SignatureException {
		signature.update(Longs.toByteArray(l));
		signature.update(longLongToByteArray(uUID.getMostSignificantBits(), uUID.getLeastSignificantBits()));
		signature.update(Longs.toByteArray(instant.getEpochSecond()));
		signature.update(string.getBytes(StandardCharsets.UTF_8));
	}

	private static byte[] longLongToByteArray(long l, long m) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
		byteBuffer.putLong(l).putLong(m);
		return byteBuffer.array();
	}

	interface ByteArrayToKeyFunction<T extends Key> {
		T apply(byte[] bs) throws CryptException;
	}

	public static record SaltSignaturePair(long salt, byte[] signature) {
		public static final Crypt.SaltSignaturePair EMPTY = new Crypt.SaltSignaturePair(0L, ByteArrays.EMPTY_ARRAY);

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
