package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseDependantDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<NoiseDependantDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.DOUBLE.fieldOf("noise_level").forGetter(noiseDependantDecoratorConfiguration -> noiseDependantDecoratorConfiguration.noiseLevel),
					Codec.INT.fieldOf("below_noise").forGetter(noiseDependantDecoratorConfiguration -> noiseDependantDecoratorConfiguration.belowNoise),
					Codec.INT.fieldOf("above_noise").forGetter(noiseDependantDecoratorConfiguration -> noiseDependantDecoratorConfiguration.aboveNoise)
				)
				.apply(instance, NoiseDependantDecoratorConfiguration::new)
	);
	public final double noiseLevel;
	public final int belowNoise;
	public final int aboveNoise;

	public NoiseDependantDecoratorConfiguration(double d, int i, int j) {
		this.noiseLevel = d;
		this.belowNoise = i;
		this.aboveNoise = j;
	}
}
