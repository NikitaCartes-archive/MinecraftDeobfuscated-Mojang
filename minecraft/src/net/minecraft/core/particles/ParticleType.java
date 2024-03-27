package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ParticleType<T extends ParticleOptions> {
	private final boolean overrideLimiter;
	private final ParticleOptions.Deserializer<T> deserializer;

	protected ParticleType(boolean bl, ParticleOptions.Deserializer<T> deserializer) {
		this.overrideLimiter = bl;
		this.deserializer = deserializer;
	}

	public boolean getOverrideLimiter() {
		return this.overrideLimiter;
	}

	public ParticleOptions.Deserializer<T> getDeserializer() {
		return this.deserializer;
	}

	public abstract MapCodec<T> codec();

	public abstract StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();
}
