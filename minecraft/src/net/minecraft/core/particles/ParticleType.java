package net.minecraft.core.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ParticleType<T extends ParticleOptions> {
	private final boolean overrideLimiter;
	private final ParticleOptions.Deserializer<T> deserializer;

	protected ParticleType(boolean bl, ParticleOptions.Deserializer<T> deserializer) {
		this.overrideLimiter = bl;
		this.deserializer = deserializer;
	}

	@Environment(EnvType.CLIENT)
	public boolean getOverrideLimiter() {
		return this.overrideLimiter;
	}

	public ParticleOptions.Deserializer<T> getDeserializer() {
		return this.deserializer;
	}
}
