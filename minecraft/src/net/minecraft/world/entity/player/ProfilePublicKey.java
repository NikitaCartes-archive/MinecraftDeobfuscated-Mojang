package net.minecraft.world.entity.player;

import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureValidator;

public record ProfilePublicKey(ProfilePublicKey.Data data) {
	public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.comapFlatMap(data -> {
		try {
			return DataResult.success(createTrusted(data));
		} catch (CryptException var2) {
			return DataResult.error("Malformed public key");
		}
	}, ProfilePublicKey::data);

	public static ProfilePublicKey createTrusted(ProfilePublicKey.Data data) throws CryptException {
		return new ProfilePublicKey(data);
	}

	public static ProfilePublicKey createValidated(SignatureValidator signatureValidator, ProfilePublicKey.Data data) throws InsecurePublicKeyException, CryptException {
		if (data.hasExpired()) {
			throw new InvalidException("Expired profile public key");
		} else if (!data.validateSignature(signatureValidator)) {
			throw new InvalidException("Invalid profile public key signature");
		} else {
			return createTrusted(data);
		}
	}

	public SignatureValidator createSignatureValidator() {
		return SignatureValidator.from(this.data.key, "SHA256withRSA");
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

		boolean validateSignature(SignatureValidator signatureValidator) {
			return signatureValidator.validate(this.signedPayload().getBytes(StandardCharsets.US_ASCII), this.keySignature);
		}

		private String signedPayload() {
			String string = Crypt.rsaPublicKeyToString(this.key);
			return this.expiresAt.toEpochMilli() + string;
		}

		public boolean hasExpired() {
			return this.expiresAt.isBefore(Instant.now());
		}
	}
}
