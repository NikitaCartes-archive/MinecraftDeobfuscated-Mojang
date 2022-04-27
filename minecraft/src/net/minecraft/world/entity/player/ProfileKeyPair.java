package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.PrivateKey;
import java.time.Instant;
import net.minecraft.util.Crypt;
import net.minecraft.util.ExtraCodecs;

public record ProfileKeyPair(PrivateKey privateKey, ProfilePublicKey publicKey, Instant refreshedAfter) {
	public static final Codec<ProfileKeyPair> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Crypt.PRIVATE_KEY_CODEC.fieldOf("private_key").forGetter(ProfileKeyPair::privateKey),
					ProfilePublicKey.CODEC.fieldOf("public_key").forGetter(ProfileKeyPair::publicKey),
					ExtraCodecs.INSTANT_ISO8601.fieldOf("refreshed_after").forGetter(ProfileKeyPair::refreshedAfter)
				)
				.apply(instance, ProfileKeyPair::new)
	);

	public boolean dueRefresh() {
		return this.refreshedAfter.isBefore(Instant.now());
	}
}
