package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoiseCountFactorDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<NoiseCountFactorDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("noise_to_count_ratio").forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseToCountRatio),
					Codec.DOUBLE.fieldOf("noise_factor").forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseFactor),
					Codec.DOUBLE
						.fieldOf("noise_offset")
						.withDefault(0.0)
						.forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.noiseOffset),
					Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(noiseCountFactorDecoratorConfiguration -> noiseCountFactorDecoratorConfiguration.heightmap)
				)
				.apply(instance, NoiseCountFactorDecoratorConfiguration::new)
	);
	private static final Logger LOGGER = LogManager.getLogger();
	public final int noiseToCountRatio;
	public final double noiseFactor;
	public final double noiseOffset;
	public final Heightmap.Types heightmap;

	public NoiseCountFactorDecoratorConfiguration(int i, double d, double e, Heightmap.Types types) {
		this.noiseToCountRatio = i;
		this.noiseFactor = d;
		this.noiseOffset = e;
		this.heightmap = types;
	}
}
