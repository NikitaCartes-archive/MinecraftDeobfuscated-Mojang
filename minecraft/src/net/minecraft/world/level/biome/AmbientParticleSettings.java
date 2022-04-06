package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

public class AmbientParticleSettings {
	public static final Codec<AmbientParticleSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ParticleTypes.CODEC.fieldOf("options").forGetter(ambientParticleSettings -> ambientParticleSettings.options),
					Codec.FLOAT.fieldOf("probability").forGetter(ambientParticleSettings -> ambientParticleSettings.probability)
				)
				.apply(instance, AmbientParticleSettings::new)
	);
	private final ParticleOptions options;
	private final float probability;

	public AmbientParticleSettings(ParticleOptions particleOptions, float f) {
		this.options = particleOptions;
		this.probability = f;
	}

	public ParticleOptions getOptions() {
		return this.options;
	}

	public boolean canSpawn(RandomSource randomSource) {
		return randomSource.nextFloat() <= this.probability;
	}
}
