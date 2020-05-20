package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SeagrassFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<SeagrassFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("count").forGetter(seagrassFeatureConfiguration -> seagrassFeatureConfiguration.count),
					Codec.DOUBLE.fieldOf("probability").forGetter(seagrassFeatureConfiguration -> seagrassFeatureConfiguration.tallSeagrassProbability)
				)
				.apply(instance, SeagrassFeatureConfiguration::new)
	);
	public final int count;
	public final double tallSeagrassProbability;

	public SeagrassFeatureConfiguration(int i, double d) {
		this.count = i;
		this.tallSeagrassProbability = d;
	}
}
