package net.minecraft.world.entity.player;

import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.time.Instant;
import java.util.UUID;
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

	public static ProfilePublicKey createValidated(SignatureValidator signatureValidator, UUID uUID, ProfilePublicKey.Data data) throws InsecurePublicKeyException, CryptException {
		if (data.hasExpired()) {
			throw new InvalidException("Expired profile public key");
		} else if (!data.validateSignature(signatureValidator, uUID)) {
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
						ExtraCodecs.BASE64_STRING.fieldOf("signature_v2").forGetter(ProfilePublicKey.Data::keySignature)
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

		boolean validateSignature(SignatureValidator signatureValidator, UUID uUID) {
			return signatureValidator.validate(this.signedPayload(uUID), this.keySignature);
		}

		private byte[] signedPayload(UUID uUID) {
			byte[] bs = this.key.getEncoded();
			byte[] cs = new byte[24 + bs.length];
			ByteBuffer byteBuffer = ByteBuffer.wrap(cs).order(ByteOrder.BIG_ENDIAN);
			byteBuffer.putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits()).putLong(this.expiresAt.toEpochMilli()).put(bs);
			return cs;
		}

		public boolean hasExpired() {
			return this.expiresAt.isBefore(Instant.now());
		}
	}
}
