package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoiseCountFactorDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<NoiseCountFactorDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("noise_to_count_ratio").forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseToCountRatio),
					Codec.DOUBLE.fieldOf("noise_factor").forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseFactor),
					Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0).forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseOffset)
				)
				.apply(instance, NoiseCountFactorDecoratorConfiguration::new)
	);
	public final int noiseToCountRatio;
	public final double noiseFactor;
	public final double noiseOffset;

	public NoiseCountFactorDecoratorConfiguration(int i, double d, double e) {
		this.noiseToCountRatio = i;
		this.noiseFactor = d;
		this.noiseOffset = e;
	}
}
