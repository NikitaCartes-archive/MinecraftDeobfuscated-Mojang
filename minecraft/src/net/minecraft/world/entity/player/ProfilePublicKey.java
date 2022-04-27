package net.minecraft.world.entity.player;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.MissingException;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(Instant expiresAt, String keyString, String signature) {
	public static final Codec<ProfilePublicKey> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey::expiresAt),
					Codec.STRING.fieldOf("key").forGetter(ProfilePublicKey::keyString),
					Codec.STRING.fieldOf("signature").forGetter(ProfilePublicKey::signature)
				)
				.apply(instance, ProfilePublicKey::new)
	);
	private static final String PROFILE_PROPERTY_KEY = "publicKey";

	public ProfilePublicKey(Pair<Instant, String> pair, String string) {
		this(pair.getFirst(), pair.getSecond(), string);
	}

	public static Optional<ProfilePublicKey> parseFromGameProfile(GameProfile gameProfile) {
		Property property = Iterables.getFirst(gameProfile.getProperties().get("publicKey"), null);
		if (property == null) {
			return Optional.empty();
		} else {
			String string = property.getValue();
			String string2 = property.getSignature();
			return !Strings.isNullOrEmpty(string) && !Strings.isNullOrEmpty(string2)
				? parsePublicKeyString(string).map(pair -> new ProfilePublicKey(pair, string2))
				: Optional.empty();
		}
	}

	public GameProfile fillGameProfile(GameProfile gameProfile) {
		gameProfile.getProperties().put("publicKey", this.property());
		return gameProfile;
	}

	public ProfilePublicKey.Trusted verify(MinecraftSessionService minecraftSessionService) throws InsecurePublicKeyException, CryptException {
		if (Strings.isNullOrEmpty(this.keyString)) {
			throw new MissingException();
		} else {
			String string = minecraftSessionService.getSecurePropertyValue(this.property());
			if (!(this.expiresAt.toEpochMilli() + this.keyString).equals(string)) {
				throw new InvalidException("Invalid profile public key signature");
			} else {
				Pair<Instant, String> pair = (Pair<Instant, String>)parsePublicKeyString(string).orElseThrow(() -> new InvalidException("Invalid profile public key"));
				if (pair.getFirst().isBefore(Instant.now())) {
					throw new InvalidException("Expired profile public key");
				} else {
					PublicKey publicKey = Crypt.stringToRsaPublicKey(pair.getSecond());
					return new ProfilePublicKey.Trusted(publicKey, this);
				}
			}
		}
	}

	private static Optional<Pair<Instant, String>> parsePublicKeyString(String string) {
		int i = string.indexOf("-----BEGIN RSA PUBLIC KEY-----");

		long l;
		try {
			l = Long.parseLong(string.substring(0, i));
		} catch (NumberFormatException var5) {
			return Optional.empty();
		}

		return Optional.of(Pair.of(Instant.ofEpochMilli(l), string.substring(i)));
	}

	public Property property() {
		return new Property("publicKey", this.expiresAt.toEpochMilli() + this.keyString, this.signature);
	}

	public boolean hasExpired() {
		return this.expiresAt.isBefore(Instant.now());
	}

	public static record Trusted(PublicKey key, ProfilePublicKey data) {
		public Signature verifySignature() throws CryptException {
			try {
				Signature signature = Signature.getInstance("SHA1withRSA");
				signature.initVerify(this.key);
				return signature;
			} catch (GeneralSecurityException var2) {
				throw new CryptException(var2);
			}
		}
	}
}
