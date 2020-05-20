package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ConfiguredFeature.CODEC
		.listOf()
		.fieldOf("features")
		.<SimpleRandomFeatureConfiguration>xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features)
		.codec();
	public final List<ConfiguredFeature<?, ?>> features;

	public SimpleRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> list) {
		this.features = list;
	}
}
