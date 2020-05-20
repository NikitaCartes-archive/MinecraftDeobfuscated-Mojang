package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class FeatureRadiusConfiguration implements FeatureConfiguration {
	public static final Codec<FeatureRadiusConfiguration> CODEC = Codec.INT
		.fieldOf("radius")
		.<FeatureRadiusConfiguration>xmap(FeatureRadiusConfiguration::new, featureRadiusConfiguration -> featureRadiusConfiguration.radius)
		.codec();
	public final int radius;

	public FeatureRadiusConfiguration(int i) {
		this.radius = i;
	}
}
