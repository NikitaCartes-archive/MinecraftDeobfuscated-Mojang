package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class CountFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<CountFeatureConfiguration> CODEC = Codec.INT
		.fieldOf("count")
		.<CountFeatureConfiguration>xmap(CountFeatureConfiguration::new, countFeatureConfiguration -> countFeatureConfiguration.count)
		.codec();
	public final int count;

	public CountFeatureConfiguration(int i) {
		this.count = i;
	}
}
