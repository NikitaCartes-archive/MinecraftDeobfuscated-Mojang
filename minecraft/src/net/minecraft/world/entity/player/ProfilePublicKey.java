package net.minecraft.world.entity.player;

import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(ProfilePublicKey.Data data) {
	public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.comapFlatMap(data -> {
		try {
			return DataResult.success(createTrusted(data));
		} catch (CryptException var2) {
			return DataResult.error("Malformed public key");
		}
	}, ProfilePublicKey::data);
	private static final String PROFILE_PROPERTY_KEY = "publicKey";

	public static ProfilePublicKey createTrusted(ProfilePublicKey.Data data) throws CryptException {
		return new ProfilePublicKey(data);
	}

	public static ProfilePublicKey createValidated(MinecraftSessionService minecraftSessionService, ProfilePublicKey.Data data) throws InsecurePublicKeyException, CryptException {
		if (data.hasExpired()) {
			throw new InvalidException("Expired profile public key");
		} else {
			String string = minecraftSessionService.getSecurePropertyValue(data.signedKeyProperty());
			if (!data.signedKeyPropertyValue().equals(string)) {
				throw new InvalidException("Invalid profile public key signature");
			} else {
				return createTrusted(data);
			}
		}
	}

	public Signature verifySignature() throws CryptException {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(this.data.key());
			return signature;
		} catch (GeneralSecurityException var2) {
			throw new CryptException(var2);
		}
	}

	public static record Data(Instant expiresAt, PublicKey key, byte[] keySignature) {
		private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
		public static final Codec<ProfilePublicKey.Data> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey.Data::expiresAt),
						Crypt.PUBLIC_KEY_CODEC.fieldOf("key").forGetter(ProfilePublicKey.Data::key),
						ExtraCodecs.BASE64_STRING.fieldOf("signature").forGetter(ProfilePublicKey.Data::keySignature)
					)
					.apply(instance, ProfilePublicKey.Data::new)
		);

		public Data(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readInstant(), friendlyByteBuf.readPublicKey(), friendlyByteBuf.readByteArray(4096));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeInstant(this.expiresAt);
			friendlyByteBuf.writePublicKey(this.key);
			friendlyByteBuf.writeByteArray(this.keySignature);
		}

		Property signedKeyProperty() {
			String string = Base64.getEncoder().encodeToString(this.keySignature);
			return new Property("publicKey", this.signedKeyPropertyValue(), string);
		}

		String signedKeyPropertyValue() {
			String string = Crypt.rsaPublicKeyToString(this.key);
			return this.expiresAt.toEpochMilli() + string;
		}

		public boolean hasExpired() {
			return this.expiresAt.isBefore(Instant.now());
		}
	}
}
