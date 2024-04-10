package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ParticleType<T extends ParticleOptions> {
	private final boolean overrideLimiter;

	protected ParticleType(boolean bl) {
		this.overrideLimiter = bl;
	}

	public boolean getOverrideLimiter() {
		return this.overrideLimiter;
	}

	public abstract MapCodec<T> codec();

	public abstract StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();
}
