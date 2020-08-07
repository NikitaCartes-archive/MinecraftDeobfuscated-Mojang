package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

public class ProbabilityFeatureConfiguration implements CarverConfiguration, FeatureConfiguration {
	public static final Codec<ProbabilityFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(probabilityFeatureConfiguration -> probabilityFeatureConfiguration.probability)
				)
				.apply(instance, ProbabilityFeatureConfiguration::new)
	);
	public final float probability;

	public ProbabilityFeatureConfiguration(float f) {
		this.probability = f;
	}
}
