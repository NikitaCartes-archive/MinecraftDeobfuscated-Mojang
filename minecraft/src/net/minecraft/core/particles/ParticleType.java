package net.minecraft.core.particles;

import java.util.Random;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ParticleType<T extends ParticleOptions> {
	private final boolean overrideLimiter;
	private final ParticleOptions.Deserializer<T> deserializer;
	private final BiFunction<Random, ParticleType<T>, T> randomOptionProvider;

	public ParticleType(boolean bl, ParticleOptions.Deserializer<T> deserializer, BiFunction<Random, ParticleType<T>, T> biFunction) {
		this.overrideLimiter = bl;
		this.deserializer = deserializer;
		this.randomOptionProvider = biFunction;
	}

	@Environment(EnvType.CLIENT)
	public boolean getOverrideLimiter() {
		return this.overrideLimiter;
	}

	public ParticleOptions.Deserializer<T> getDeserializer() {
		return this.deserializer;
	}

	public T getRandom(Random random) {
		return (T)this.randomOptionProvider.apply(random, this);
	}
}
